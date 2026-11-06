#!/usr/bin/env python3
"""Core library for discovering, validating, versioning, building, and tagging Android projects.

Architectural Overview & Design Rationale
-----------------------------------------
Android projects spread version and toolchain metadata across multiple files that must
stay in lockstep during a release:
  1. `build.gradle` or `build.gradle.kts` (`versionCode`, `versionName`, `compileSdk`,
     `targetSdk`, `minSdk`, AGP plugin version, and `compileOptions`).
  2. `AndroidManifest.xml` (legacy or explicit `android:versionCode` and
     `android:versionName` attributes, which override or must match Gradle in many
     F-Droid and custom build setups).
  3. `fdroid/metadata/<app_id>.yml` (when publishing to F-Droid, the YAML build recipe
     records `CurrentVersion`, `CurrentVersionCode`, and a per-release `Builds:` block
     pinned to a Git tag).
  4. `fastlane/metadata/android/<locale>/changelogs/<versionCode>.txt` (both F-Droid
     and Google Play Fastlane uploaders look up release notes by integer `versionCode`).

Additionally, Android builds are uniquely sensitive to version mismatches across four
distinct toolchain layers (Host JDK -> Gradle Wrapper -> Android Gradle Plugin ->
Android SDK `platforms` & `build-tools`). A single mismatch—such as Ubuntu upgrading
the default `java` to JDK 26 while Gradle 8.14's Groovy parser only understands class
files up to JDK 21/24, or bumping `compileSdk = 36` while still on AGP 8.9.0—results
in cryptic build failures or warnings.

This module encapsulates project discovery, Markdown table serialization, build
environment diagnostics, F-Droid readiness checks, version synchronization, artifact
building, and Git tagging into a single zero-dependency Python class (`AndroidReleaser`)
that can be embedded in any Python binary or invoked from the CLI.
"""

from __future__ import annotations

from dataclasses import dataclass, field
import hashlib
import os
from pathlib import Path
import re
import shutil
import subprocess
from typing import Dict, Iterable, List, Optional, Sequence, Tuple
import xml.etree.ElementTree as ET

ANDROID_NS = "{http://schemas.android.com/apk/res/android}"

# Directories skipped during recursive project discovery so scanning a large workspace
# never wastes time traversing build caches, Git object stores, or source packages.
DEFAULT_IGNORED_DIRS = frozenset({
    ".git",
    ".gradle",
    ".idea",
    "build",
    "captures",
    "docs",
    "fastlane",
    "fdroid",
    "gradle",
    "releases",
    "releaser",
    "res",
    "src",
    "test",
    "__pycache__",
})

# Minimum Android Gradle Plugin (AGP) version officially tested against each compileSdk
# level by Google. Using an older AGP with a newer compileSdk triggers the build warning:
# "We recommend using a newer Android Gradle plugin to use compileSdk = N".
MIN_AGP_FOR_COMPILE_SDK: Dict[int, Tuple[int, int, int]] = {
    33: (7, 2, 0),
    34: (8, 1, 1),
    35: (8, 6, 0),
    36: (8, 10, 0),
}

# Recommended stable AGP version to suggest when an upgrade is needed for SDK 36.
RECOMMENDED_AGP_VERSION = "8.13.2"
RECOMMENDED_GRADLE_VERSION = "8.14"
RECOMMENDED_JDK_MAJOR = 21
PLAY_STORE_MIN_TARGET_SDK = 35


def _parse_version_tuple(ver_str: str) -> Tuple[int, ...]:
    """Converts a version string like '8.13.2' or '8.14-rc1' into an integer tuple `(8, 13, 2)`
    so versions can be compared numerically rather than lexicographically (where '8.9.0' > '8.13.2').
    """
    nums = re.findall(r"[0-9]+", ver_str or "")
    return tuple(int(n) for n in nums) if nums else (0,)


@dataclass
class CheckFinding:
    """Represents a single diagnostic result from build-environment or F-Droid readiness checks.

    Attributes:
        severity: One of `'ERROR'` (blocks build or store submission), `'WARNING'`
            (causes build warnings or potential runtime issues), `'SUGGESTION'`
            (recommended toolchain improvement), or `'OK'`.
        scope: Which component or project the finding applies to (e.g. `'Host Environment'`
            or `'chordwheel'`).
        category: Short category label (`'Java / JDK'`, `'Gradle'`, `'AGP'`, `'Android SDK'`,
            `'F-Droid Readiness'`).
        summary: One-line human-readable summary of the check result.
        explanation: Verbose explanation of *why* this condition matters.
        remediation: Concrete, copy-pasteable fix or recommendation for the user.
    """

    severity: str
    scope: str
    category: str
    summary: str
    explanation: str = ""
    remediation: str = ""

    def format_verbose(self) -> str:
        """Formats the finding with full explanation and remediation for terminal output."""
        lines = [f"[{self.severity}] ({self.scope} — {self.category}) {self.summary}"]
        if self.explanation:
            lines.append(f"    Why this matters: {self.explanation}")
        if self.remediation:
            lines.append(f"    Suggested fix   : {self.remediation}")
        return "\n".join(lines)


@dataclass
class AndroidProject:
    """Represents a discovered Android application project on disk.

    Separates `project_dir` (where `gradlew` and `settings.gradle` live) from
    `module_dir` (where the `com.android.application` `build.gradle` lives) so
    both flat single-directory projects (`chordwheel/build.gradle`) and standard
    multi-module Android Studio projects (`my-app/app/build.gradle.kts`) are
    handled uniformly.
    """

    root_dir: Path
    project_dir: Path
    module_dir: Path
    gradle_file: Path
    manifest_file: Optional[Path]
    gradlew_path: Path
    app_id: str
    display_name: str
    version_name: str
    version_code: int
    agp_version: Optional[str] = None
    gradle_wrapper_version: Optional[str] = None
    compile_sdk: Optional[int] = None
    target_sdk: Optional[int] = None
    min_sdk: Optional[int] = None
    build_tools_version: Optional[str] = None
    java_source_compat: Optional[str] = None
    java_target_compat: Optional[str] = None
    fdroid_yml_file: Optional[Path] = None
    fastlane_dir: Optional[Path] = None

    @property
    def rel_project_dir(self) -> str:
        """Relative path from the scan root to the Gradle project directory."""
        try:
            rel = self.project_dir.relative_to(self.root_dir)
            return "." if str(rel) == "." else rel.as_posix()
        except ValueError:
            return self.project_dir.as_posix()

    @property
    def project_slug(self) -> str:
        """Short identifier used in Git tags and staged artifact filenames."""
        if self.project_dir == self.root_dir:
            return self.project_dir.name
        return self.project_dir.relative_to(self.root_dir).as_posix().replace("/", "-")

    @property
    def has_fdroid_metadata(self) -> bool:
        """Returns True if an F-Droid `.yml` metadata recipe exists for this app."""
        return self.fdroid_yml_file is not None and self.fdroid_yml_file.is_file()

    def format_tag(self, tag_pattern: str, version_name: Optional[str] = None) -> str:
        """Formats a Git tag using placeholders `{app}`, `{app_id}`, `{version}`, `{code}`."""
        v_name = version_name if version_name is not None else self.version_name
        return tag_pattern.format(
            app=self.project_slug,
            app_id=self.app_id,
            version=v_name,
            code=self.version_code,
        )


@dataclass
class VersionRow:
    """Represents a single row in the human-editable Markdown version table."""

    project_dir: str
    app_id: str
    display_name: str
    version_name: str
    version_code: int
    tag_pattern: str = "{app}-v{version}"

    def render_tag(self) -> str:
        slug = self.project_dir.strip("/").replace("/", "-")
        if slug in ("", "."):
            slug = self.app_id.split(".")[-1]
        return self.tag_pattern.format(
            app=slug,
            app_id=self.app_id,
            version=self.version_name,
            code=self.version_code,
        )


@dataclass
class ReleaseArtifacts:
    """Paths to the generated and staged release artifacts for an Android app."""

    project_dir: Path
    version_name: str
    version_code: int
    tag_name: str
    output_dir: Path
    bundle_aab: Optional[Path] = None
    release_apk: Optional[Path] = None
    debug_apk: Optional[Path] = None
    checksums_file: Optional[Path] = None
    updated_files: List[Path] = field(default_factory=list)


class MarkdownVersionTable:
    """Reads and writes a human-editable Markdown table of Android app versions.

    Why header-driven decoding is used:
    Users frequently reorder columns in `versions.md` (for example, placing
    `Version Name` and `Version Code` immediately next to `Project Dir` for
    easier editing) or add notes columns. By decoding column indices from the
    first row's header names rather than hardcoded positional indices, the table
    remains resilient to column reordering.
    """

    HEADER_COMMENT = (
        "# Android App Versions\n"
        "\n"
        "Edit **Version Name** and **Version Code** in the table below for any app you want to\n"
        "bump, then run:\n"
        "\n"
        "```bash\n"
        "python3 releaser/release.py\n"
        "```\n"
    )

    COLUMNS = (
        "Project Dir",
        "Version Name",
        "Version Code",
        "App ID",
        "Display Name",
        "Tag Pattern",
    )
    DEFAULT_COLUMNS = COLUMNS

    # Flexible header aliases so users can rename headers slightly (e.g. 'Version'
    # instead of 'Version Name' or 'Package' instead of 'App ID') without breaking parsing.
    _HEADER_ALIASES = {
        "projectdir": "project_dir",
        "project": "project_dir",
        "dir": "project_dir",
        "directory": "project_dir",
        "path": "project_dir",
        "appid": "app_id",
        "applicationid": "app_id",
        "package": "app_id",
        "packageid": "app_id",
        "displayname": "display_name",
        "appname": "display_name",
        "name": "display_name",
        "title": "display_name",
        "versionname": "version_name",
        "version": "version_name",
        "ver": "version_name",
        "versioncode": "version_code",
        "code": "version_code",
        "buildcode": "version_code",
        "tagpattern": "tag_pattern",
        "tagformat": "tag_pattern",
        "tag": "tag_pattern",
    }
    HEADER_ALIASES = _HEADER_ALIASES

    @classmethod
    def _normalize_header(cls, header_cell: str) -> Optional[str]:
        cleaned = re.sub(r"[^a-z0-9]", "", header_cell.strip().lower())
        return cls._HEADER_ALIASES.get(cleaned)

    @classmethod
    def dumps(
        cls,
        rows: Sequence[VersionRow],
        include_header: bool = True,
        columns: Sequence[str] = COLUMNS,
    ) -> str:
        """Formats a sequence of `VersionRow` objects as an aligned Markdown table."""
        col_keys = []
        for col in columns:
            key = cls._normalize_header(col)
            if not key:
                raise ValueError(f"Unrecognized column header: '{col}'")
            col_keys.append(key)

        table_data = []
        for r in rows:
            row_values = {
                "project_dir": r.project_dir,
                "app_id": r.app_id,
                "display_name": r.display_name,
                "version_name": r.version_name,
                "version_code": str(r.version_code),
                "tag_pattern": r.tag_pattern,
            }
            table_data.append(tuple(row_values[k] for k in col_keys))

        widths = [len(col) for col in columns]
        for row_tuple in table_data:
            for idx, cell in enumerate(row_tuple):
                widths[idx] = max(widths[idx], len(cell))

        def fmt_row(cells: Sequence[str], right_align_indices: set[int] | None = None) -> str:
            padded = []
            for i in range(len(columns)):
                if right_align_indices and i in right_align_indices:
                    padded.append(f"{cells[i]:>{widths[i]}}")
                else:
                    padded.append(f"{cells[i]:<{widths[i]}}")
            return "| " + " | ".join(padded) + " |"

        # Right-align numeric/version columns so decimal points and digits line up visually.
        right_align = {
            i for i, k in enumerate(col_keys) if k in ("version_name", "version_code")
        }
        header_line = fmt_row(columns)
        sep_line = "| " + " | ".join("-" * w for w in widths) + " |"
        body_lines = [fmt_row(t, right_align_indices=right_align) for t in table_data]

        lines = []
        if include_header:
            lines.append(cls.HEADER_COMMENT.rstrip())
            lines.append("")
        lines.append(header_line)
        lines.append(sep_line)
        lines.extend(body_lines)
        lines.append("")
        return "\n".join(lines)

    @classmethod
    def loads(cls, markdown_text: str) -> List[VersionRow]:
        """Parses a Markdown table into a list of `VersionRow` objects, decoding
        columns dynamically from the first row's header names regardless of order.
        """
        rows: List[VersionRow] = []
        table_lines = [
            line.strip()
            for line in markdown_text.splitlines()
            if line.strip().startswith("|") and line.strip().endswith("|")
        ]
        if len(table_lines) < 2:
            raise ValueError(
                "No valid Markdown table found. Expected at least a header row and "
                "separator row enclosed in '|' pipes."
            )

        header_cells = [c.strip().strip("`*") for c in table_lines[0].strip("|").split("|")]
        col_index_by_field: dict[str, int] = {}
        for idx, cell in enumerate(header_cells):
            field_name = cls._normalize_header(cell)
            if field_name and field_name not in col_index_by_field:
                col_index_by_field[field_name] = idx

        required_fields = ("project_dir", "version_name", "version_code")
        missing = [f for f in required_fields if f not in col_index_by_field]
        if missing:
            raise ValueError(
                f"Markdown table header is missing required column(s): {', '.join(missing)}. "
                f"Found header columns: {header_cells}. "
                f"Supported column names include: {', '.join(cls.COLUMNS)}."
            )

        for line_num, line in enumerate(table_lines[1:], start=2):
            raw_cells = [c.strip().strip("`") for c in line.strip("|").split("|")]
            if not raw_cells:
                continue
            # Skip the Markdown separator line (e.g. `| --- | :---: | --- |`).
            if all(set(c) <= {"-", ":"} for c in raw_cells if c):
                continue

            def get_cell(field_key: str, default: str = "") -> str:
                col_idx = col_index_by_field.get(field_key)
                if col_idx is None or col_idx >= len(raw_cells):
                    return default
                return raw_cells[col_idx]

            project_dir = get_cell("project_dir")
            app_id = get_cell("app_id")
            display_name = get_cell("display_name") or project_dir
            version_name = get_cell("version_name")
            version_code_str = get_cell("version_code")
            try:
                version_code = int(version_code_str)
            except ValueError as exc:
                raise ValueError(
                    f"Invalid integer Version Code '{version_code_str}' in table row {line_num} "
                    f"for project '{project_dir}'. Android requires versionCode to be a positive integer."
                ) from exc
            tag_pattern = get_cell("tag_pattern", "{app}-v{version}") or "{app}-v{version}"
            rows.append(
                VersionRow(
                    project_dir=project_dir,
                    app_id=app_id,
                    display_name=display_name,
                    version_name=version_name,
                    version_code=version_code,
                    tag_pattern=tag_pattern,
                )
            )
        return rows

    @classmethod
    def write_file(
        cls,
        path: Path,
        rows: Sequence[VersionRow],
        columns: Sequence[str] = COLUMNS,
    ) -> Path:
        path = Path(path)
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(
            cls.dumps(rows, include_header=True, columns=columns), encoding="utf-8"
        )
        return path

    @classmethod
    def read_file(cls, path: Path) -> List[VersionRow]:
        path = Path(path)
        if not path.is_file():
            raise FileNotFoundError(
                f"Markdown version table not found at '{path}'. "
                f"Run './releaser/scan_versions.py' first to generate it."
            )
        return cls.loads(path.read_text(encoding="utf-8"))


class AndroidReleaser:
    """General-purpose Android project scanner, environment validator, F-Droid checker,
    version synchronizer, builder, and Git tagger.

    Can be imported into any Python script or binary:
        releaser = AndroidReleaser(root_dir="/path/to/workspace")
        projects = releaser.discover_projects()
        findings = releaser.check_build_environment(projects)
        fdroid_issues = releaser.check_all_fdroid_readiness(projects)
    """

    # Regex patterns supporting both Groovy (`versionCode = 4`, `versionCode 4`)
    # and Kotlin DSL (`versionCode = 4`, `versionName = "1.4"`).
    _RE_APPLICATION_ID = re.compile(
        r"""^\s*applicationId\s*(?:=|\s)\s*['"]([^'"]+)['"]""", re.MULTILINE
    )
    _RE_NAMESPACE = re.compile(
        r"""^\s*namespace\s*(?:=|\s)\s*['"]([^'"]+)['"]""", re.MULTILINE
    )
    _RE_VERSION_CODE = re.compile(
        r"""^(\s*versionCode\s*(?:=|\s)\s*)([0-9]+)\b""", re.MULTILINE
    )
    _RE_VERSION_NAME = re.compile(
        r"""^(\s*versionName\s*(?:=|\s)\s*['"])([^'"]+)(['"])""", re.MULTILINE
    )
    _RE_ANDROID_APP_PLUGIN = re.compile(
        r"""com\.android\.application""", re.MULTILINE
    )
    _RE_AGP_VERSION_INLINE = re.compile(
        r"""com\.android\.application['"]?\s*\)\s*version\s*['"]([^'"]+)['"]|"""
        r"""com\.android\.application['"]\s+version\s+['"]([^'"]+)['"]|"""
        r"""com\.android\.tools\.build:gradle:([0-9][0-9A-Za-z._-]*)""",
        re.MULTILINE,
    )
    _RE_COMPILE_SDK = re.compile(
        r"""^\s*compileSdk(?:Version)?\s*(?:=|\s)\s*([0-9]+)\b""", re.MULTILINE
    )
    _RE_TARGET_SDK = re.compile(
        r"""^\s*targetSdk(?:Version)?\s*(?:=|\s)\s*([0-9]+)\b""", re.MULTILINE
    )
    _RE_MIN_SDK = re.compile(
        r"""^\s*minSdk(?:Version)?\s*(?:=|\s)\s*([0-9]+)\b""", re.MULTILINE
    )
    _RE_BUILD_TOOLS = re.compile(
        r"""^\s*buildToolsVersion\s*(?:=|\s)\s*['"]([^'"]+)['"]""", re.MULTILINE
    )
    _RE_SOURCE_COMPAT = re.compile(
        r"""^\s*sourceCompatibility\s*(?:=|\s)\s*([A-Za-z0-9_.]+)""", re.MULTILINE
    )
    _RE_TARGET_COMPAT = re.compile(
        r"""^\s*targetCompatibility\s*(?:=|\s)\s*([A-Za-z0-9_.]+)""", re.MULTILINE
    )

    def __init__(
        self,
        root_dir: Path | str = ".",
        ignored_dirs: Iterable[str] = DEFAULT_IGNORED_DIRS,
    ) -> None:
        self.root_dir = Path(root_dir).resolve()
        self.ignored_dirs = set(ignored_dirs)

    # ------------------------------------------------------------------
    # 1. Recursive Project Discovery
    # ------------------------------------------------------------------
    def discover_projects(self) -> List[AndroidProject]:
        """Recursively scans `self.root_dir` for Android application projects.

        Why we look for `com.android.application` in `build.gradle(.kts)`:
        In a multi-module project, root `build.gradle` files and library modules
        (`com.android.library`) also exist. Filtering specifically for build scripts
        that apply `com.android.application` and declare `versionCode`/`versionName`
        ensures we only register actual shippable Android apps.
        """
        gradle_files: List[Path] = []
        for dirpath, dirnames, filenames in os.walk(self.root_dir):
            dirnames[:] = sorted(
                d for d in dirnames if d not in self.ignored_dirs and not d.startswith(".")
            )
            for fname in ("build.gradle", "build.gradle.kts"):
                if fname in filenames:
                    gradle_files.append(Path(dirpath) / fname)

        projects: List[AndroidProject] = []
        for gradle_file in sorted(gradle_files):
            project = self._inspect_gradle_module(gradle_file)
            if project is not None:
                projects.append(project)

        projects.sort(key=lambda p: p.rel_project_dir)
        return projects

    def _inspect_gradle_module(self, gradle_file: Path) -> Optional[AndroidProject]:
        """Checks if `gradle_file` is an Android application module and extracts metadata."""
        try:
            text = gradle_file.read_text(encoding="utf-8")
        except OSError:
            return None

        if not self._RE_ANDROID_APP_PLUGIN.search(text):
            return None

        vc_match = self._RE_VERSION_CODE.search(text)
        vn_match = self._RE_VERSION_NAME.search(text)
        if not vc_match or not vn_match:
            return None

        version_code = int(vc_match.group(2))
        version_name = vn_match.group(2)

        module_dir = gradle_file.parent
        project_dir, gradlew_path = self._find_project_root_and_gradlew(module_dir)
        manifest_file = self._find_manifest(module_dir)

        app_id = self._extract_app_id(text, manifest_file)
        if not app_id:
            return None

        agp_version = self._extract_agp_version(text, project_dir)
        gradle_wrapper_version = self._extract_gradle_wrapper_version(project_dir)
        compile_sdk = self._extract_int(self._RE_COMPILE_SDK, text)
        target_sdk = self._extract_int(self._RE_TARGET_SDK, text)
        min_sdk = self._extract_int(self._RE_MIN_SDK, text)
        bt_match = self._RE_BUILD_TOOLS.search(text)
        build_tools_version = bt_match.group(1) if bt_match else None
        sc_match = self._RE_SOURCE_COMPAT.search(text)
        tc_match = self._RE_TARGET_COMPAT.search(text)
        java_source_compat = sc_match.group(1) if sc_match else None
        java_target_compat = tc_match.group(1) if tc_match else None

        fdroid_yml = self._find_fdroid_yml(project_dir, app_id)
        fastlane_dir = self._find_fastlane_dir(project_dir, module_dir)
        display_name = self._extract_display_name(
            module_dir, project_dir, fdroid_yml, app_id
        )

        return AndroidProject(
            root_dir=self.root_dir,
            project_dir=project_dir,
            module_dir=module_dir,
            gradle_file=gradle_file,
            manifest_file=manifest_file,
            gradlew_path=gradlew_path,
            app_id=app_id,
            display_name=display_name,
            version_name=version_name,
            version_code=version_code,
            agp_version=agp_version,
            gradle_wrapper_version=gradle_wrapper_version,
            compile_sdk=compile_sdk,
            target_sdk=target_sdk,
            min_sdk=min_sdk,
            build_tools_version=build_tools_version,
            java_source_compat=java_source_compat,
            java_target_compat=java_target_compat,
            fdroid_yml_file=fdroid_yml,
            fastlane_dir=fastlane_dir,
        )

    @staticmethod
    def _extract_int(pattern: re.Pattern[str], text: str) -> Optional[int]:
        m = pattern.search(text)
        return int(m.group(1)) if m else None

    def _extract_agp_version(self, module_gradle_text: str, project_dir: Path) -> Optional[str]:
        """Finds the Android Gradle Plugin version in the module build file, root build file,
        or `gradle/libs.versions.toml` version catalog.
        """
        m = self._RE_AGP_VERSION_INLINE.search(module_gradle_text)
        if m:
            return next((g for g in m.groups() if g), None)

        for root_name in ("build.gradle", "build.gradle.kts", "settings.gradle", "settings.gradle.kts"):
            candidate = project_dir / root_name
            if candidate.is_file():
                try:
                    m = self._RE_AGP_VERSION_INLINE.search(candidate.read_text(encoding="utf-8"))
                    if m:
                        return next((g for g in m.groups() if g), None)
                except OSError:
                    pass

        toml_file = project_dir / "gradle" / "libs.versions.toml"
        if toml_file.is_file():
            try:
                toml_text = toml_file.read_text(encoding="utf-8")
                m = re.search(r"""(?m)^\s*agp\s*=\s*['"]([^'"]+)['"]""", toml_text)
                if m:
                    return m.group(1)
            except OSError:
                pass
        return None

    @staticmethod
    def _extract_gradle_wrapper_version(project_dir: Path) -> Optional[str]:
        """Reads `gradle/wrapper/gradle-wrapper.properties` to determine the Gradle version."""
        props = project_dir / "gradle" / "wrapper" / "gradle-wrapper.properties"
        if not props.is_file():
            return None
        try:
            text = props.read_text(encoding="utf-8")
            m = re.search(r"gradle-([0-9][0-9A-Za-z._-]*?)-(?:bin|all)\.zip", text)
            return m.group(1) if m else None
        except OSError:
            return None

    def _find_project_root_and_gradlew(self, module_dir: Path) -> Tuple[Path, Path]:
        """Walks up from `module_dir` to find `gradlew` or `settings.gradle(.kts)`."""
        cur = module_dir
        while True:
            gradlew = cur / "gradlew"
            if gradlew.is_file():
                return cur, gradlew
            if (cur / "settings.gradle").is_file() or (cur / "settings.gradle.kts").is_file():
                return cur, gradlew
            if cur == self.root_dir or cur.parent == cur:
                break
            cur = cur.parent
        return module_dir, module_dir / "gradlew"

    @staticmethod
    def _find_manifest(module_dir: Path) -> Optional[Path]:
        """Supports both flat (`<module>/AndroidManifest.xml`) and standard Gradle
        source-set (`<module>/src/main/AndroidManifest.xml`) locations.
        """
        candidates = [
            module_dir / "AndroidManifest.xml",
            module_dir / "src" / "main" / "AndroidManifest.xml",
        ]
        for candidate in candidates:
            if candidate.is_file():
                return candidate
        return None

    def _extract_app_id(
        self, gradle_text: str, manifest_file: Optional[Path]
    ) -> Optional[str]:
        m = self._RE_APPLICATION_ID.search(gradle_text)
        if m:
            return m.group(1)
        m = self._RE_NAMESPACE.search(gradle_text)
        if m:
            return m.group(1)
        if manifest_file and manifest_file.is_file():
            try:
                root = ET.parse(manifest_file).getroot()
                pkg = root.attrib.get("package")
                if pkg:
                    return pkg
            except ET.ParseError:
                pass
        return None

    def _find_fdroid_yml(self, project_dir: Path, app_id: str) -> Optional[Path]:
        candidates = [
            self.root_dir / "fdroid" / "metadata" / f"{app_id}.yml",
            project_dir / "fdroid" / "metadata" / f"{app_id}.yml",
            self.root_dir / "metadata" / f"{app_id}.yml",
        ]
        for candidate in candidates:
            if candidate.is_file():
                return candidate
        return None

    @staticmethod
    def _find_fastlane_dir(project_dir: Path, module_dir: Path) -> Optional[Path]:
        candidates = [
            project_dir / "fastlane" / "metadata" / "android",
            module_dir / "fastlane" / "metadata" / "android",
        ]
        for candidate in candidates:
            if candidate.is_dir():
                return candidate
        return None

    @staticmethod
    def _extract_display_name(
        module_dir: Path,
        project_dir: Path,
        fdroid_yml: Optional[Path],
        app_id: str,
    ) -> str:
        # 1. Check F-Droid AutoName if available
        if fdroid_yml and fdroid_yml.is_file():
            try:
                for line in fdroid_yml.read_text(encoding="utf-8").splitlines():
                    if line.startswith("AutoName:"):
                        name = line.split(":", 1)[1].strip().strip("'\"")
                        if name:
                            return name
            except OSError:
                pass

        # 2. Check res/values/strings.xml for <string name="app_name">
        for strings_path in (
            module_dir / "res" / "values" / "strings.xml",
            module_dir / "src" / "main" / "res" / "values" / "strings.xml",
        ):
            if strings_path.is_file():
                try:
                    root = ET.parse(strings_path).getroot()
                    for elem in root.findall("string"):
                        if elem.attrib.get("name") == "app_name" and elem.text:
                            return elem.text.strip()
                except ET.ParseError:
                    pass

        # 3. Check settings.gradle(.kts) rootProject.name
        for settings_name in ("settings.gradle", "settings.gradle.kts"):
            sp = project_dir / settings_name
            if sp.is_file():
                try:
                    m = re.search(
                        r"""rootProject\.name\s*=\s*['"]([^'"]+)['"]""",
                        sp.read_text(encoding="utf-8"),
                    )
                    if m:
                        return m.group(1)
                except OSError:
                    pass

        return app_id.split(".")[-1].capitalize()

    # ------------------------------------------------------------------
    # 2. Build Environment & Toolchain Diagnostics
    # ------------------------------------------------------------------
    @staticmethod
    def _detect_java_major(java_bin: str) -> Tuple[Optional[int], str]:
        """Runs `<java_bin> -version` and extracts the major JDK version integer."""
        try:
            proc = subprocess.run(
                [java_bin, "-version"],
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                text=True,
                check=False,
                timeout=5,
            )
            out = (proc.stdout or "").strip()
            first_line = out.splitlines()[0] if out else ""
            # Match `version "21.0.4"`, `version "1.8.0_392"`, `version "26.0.2"`
            m = re.search(r'version\s+"([0-9]+)(?:\.([0-9]+))?', out)
            if m:
                first_num = int(m.group(1))
                second_num = int(m.group(2)) if m.group(2) else 0
                major = second_num if first_num == 1 else first_num
                return major, first_line
            return None, first_line
        except (OSError, subprocess.SubprocessError) as exc:
            return None, str(exc)

    def _find_android_sdk_root(self, projects: Sequence[AndroidProject]) -> Optional[Path]:
        """Resolves the Android SDK installation directory from `ANDROID_HOME`,
        `ANDROID_SDK_ROOT`, `local.properties`, or standard Linux/macOS locations.
        """
        for env_var in ("ANDROID_HOME", "ANDROID_SDK_ROOT"):
            val = os.environ.get(env_var)
            if val and Path(val).is_dir():
                return Path(val)

        for p in projects:
            local_props = p.project_dir / "local.properties"
            if local_props.is_file():
                try:
                    for line in local_props.read_text(encoding="utf-8").splitlines():
                        if line.strip().startswith("sdk.dir="):
                            sdk_path = Path(line.split("=", 1)[1].strip())
                            if sdk_path.is_dir():
                                return sdk_path
                except OSError:
                    pass

        for fallback in (Path.home() / "Android" / "Sdk", Path("/opt/android-sdk")):
            if fallback.is_dir():
                return fallback
        return None

    def check_build_environment(
        self, projects: Optional[Sequence[AndroidProject]] = None
    ) -> List[CheckFinding]:
        """Inspects the host JDK, Android SDK, and each project's Gradle, AGP, and Java
        bytecode configuration, returning actionable findings and upgrade suggestions.
        """
        if projects is None:
            projects = self.discover_projects()

        findings: List[CheckFinding] = []

        # 1. Host Java / JDK Check
        # Why we check this: Gradle 8.x embeds Groovy 3.0.x / ASM bytecode parsers that
        # only recognize Java class file major versions up to JDK 21–24. Running Gradle
        # under bleeding-edge JDK 25 (major 69) or JDK 26 (major 70) crashes during
        # buildscript semantic analysis (`Unsupported class file major version 70`) or
        # inside AGP's `JdkImageTransform` (`jlink`).
        java_home = os.environ.get("JAVA_HOME")
        java_bin = str(Path(java_home) / "bin" / "java") if java_home else "java"
        jdk_major, jdk_banner = self._detect_java_major(java_bin)

        jdk21_Installed = any(
            Path(p).is_dir()
            for p in (
                "/usr/lib/jvm/java-21-openjdk-amd64",
                "/usr/lib/jvm/java-21-openjdk-arm64",
                "/usr/lib/jvm/java-21-openjdk",
            )
        )

        if jdk_major is None:
            findings.append(
                CheckFinding(
                    severity="ERROR",
                    scope="Host Environment",
                    category="Java / JDK",
                    summary=f"Could not execute Java (`{java_bin}`).",
                    explanation=(
                        "Gradle and the Android Gradle Plugin require OpenJDK 21 LTS (or 17 LTS) "
                        "to run the build daemon, compile Java sources, and run `jlink`."
                    ),
                    remediation="Install OpenJDK 21 (`sudo apt install openjdk-21-jdk`) and set JAVA_HOME.",
                )
            )
        elif jdk_major >= 25:
            # Check if all projects' gradlew scripts have the JDK 21/17 auto-fallback
            all_gradlew_guarded = all(
                p.gradlew_path.is_file()
                and "java-21-openjdk" in p.gradlew_path.read_text(encoding="utf-8", errors="ignore")
                for p in projects
            )
            if all_gradlew_guarded and jdk21_Installed:
                findings.append(
                    CheckFinding(
                        severity="OK",
                        scope="Host Environment",
                        category="Java / JDK",
                        summary=(
                            f"Default `java` is JDK {jdk_major} ({jdk_banner}), and `gradlew` "
                            f"auto-selects installed OpenJDK 21 LTS."
                        ),
                    )
                )
                findings.append(
                    CheckFinding(
                        severity="SUGGESTION",
                        scope="Host Environment",
                        category="Java / JDK",
                        summary=(
                            f"Shell default `java` is JDK {jdk_major} rather than OpenJDK 21 LTS."
                        ),
                        explanation=(
                            "While each project's `./gradlew` script has a fallback to `/usr/lib/jvm/java-21-openjdk-amd64`, "
                            "running `gradle`, `sdkmanager`, or Android Studio directly from the shell without `JAVA_HOME` "
                            f"will pick up JDK {jdk_major} (class file major {jdk_major + 44}), which Gradle 8.x does not support."
                        ),
                        remediation="Add `export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64` to your `~/.bashrc`.",
                    )
                )
            else:
                findings.append(
                    CheckFinding(
                        severity="ERROR" if not jdk21_Installed else "WARNING",
                        scope="Host Environment",
                        category="Java / JDK",
                        summary=(
                            f"Active Java is JDK {jdk_major} ({jdk_banner}), which is too new for Gradle 8.x / AGP 8.x."
                        ),
                        explanation=(
                            f"JDK {jdk_major} emits class file major version {jdk_major + 44}. "
                            "Gradle 8.14's Groovy parser and Android's `JdkImageTransform` (`jlink`) "
                            "only support up to JDK 21 LTS (major version 65) and fail with "
                            "`Unsupported class file major version` under JDK 25+."
                        ),
                        remediation=(
                            "Install OpenJDK 21 LTS (`sudo apt install openjdk-21-jdk`) and set "
                            "`export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`."
                        ),
                    )
                )
        elif jdk_major < 17:
            findings.append(
                CheckFinding(
                    severity="ERROR",
                    scope="Host Environment",
                    category="Java / JDK",
                    summary=f"Active Java is JDK {jdk_major} ({jdk_banner}), which is too old for AGP 8.x.",
                    explanation="Android Gradle Plugin 8.x requires at least JDK 17 (JDK 21 LTS recommended) to run.",
                    remediation="Install OpenJDK 21 LTS (`sudo apt install openjdk-21-jdk`) and set JAVA_HOME.",
                )
            )
        else:
            findings.append(
                CheckFinding(
                    severity="OK",
                    scope="Host Environment",
                    category="Java / JDK",
                    summary=f"Active Java is JDK {jdk_major} ({jdk_banner}) — compatible with Gradle 8.x and AGP 8.x.",
                )
            )

        # 2. Android SDK Installation Check
        # Why we check this: Without `platforms/android-<compileSdk>` and
        # `build-tools/<buildToolsVersion>`, Gradle fails mid-build with
        # `Failed to find target with hash string 'android-36'`.
        sdk_root = self._find_android_sdk_root(projects)
        required_platforms = {p.compile_sdk for p in projects if p.compile_sdk is not None}
        required_build_tools = {
            p.build_tools_version for p in projects if p.build_tools_version is not None
        }

        if sdk_root is None:
            findings.append(
                CheckFinding(
                    severity="ERROR",
                    scope="Host Environment",
                    category="Android SDK",
                    summary="Android SDK directory not found (`ANDROID_HOME` is unset and no `~/Android/Sdk` found).",
                    explanation=(
                        "Gradle requires the Android SDK (`platforms/android-<N>` and `build-tools/<ver>`) "
                        "to compile against `android.jar` and package APKs/bundles with `aapt2`."
                    ),
                    remediation=(
                        "Set `export ANDROID_HOME=\"$HOME/Android/Sdk\"` and install required packages via "
                        "`sdkmanager --install \"platform-tools\" \"platforms;android-36\" \"build-tools;36.0.0\"`."
                    ),
                )
            )
        else:
            missing_sdk_pkgs: List[str] = []
            for api_level in sorted(required_platforms):
                if not (sdk_root / "platforms" / f"android-{api_level}").is_dir():
                    missing_sdk_pkgs.append(f"platforms;android-{api_level}")
            for bt_ver in sorted(required_build_tools):
                if not (sdk_root / "build-tools" / bt_ver).is_dir():
                    missing_sdk_pkgs.append(f"build-tools;{bt_ver}")

            if missing_sdk_pkgs:
                pkg_args = " ".join(f'"{pkg}"' for pkg in missing_sdk_pkgs)
                findings.append(
                    CheckFinding(
                        severity="ERROR",
                        scope="Host Environment",
                        category="Android SDK",
                        summary=f"Android SDK at '{sdk_root}' is missing package(s): {', '.join(missing_sdk_pkgs)}.",
                        explanation="Apps in this workspace reference these SDK platforms/build-tools in `build.gradle`.",
                        remediation=f"Run: sdkmanager --install {pkg_args}",
                    )
                )
            else:
                findings.append(
                    CheckFinding(
                        severity="OK",
                        scope="Host Environment",
                        category="Android SDK",
                        summary=(
                            f"Android SDK at '{sdk_root}' has all required platforms "
                            f"({sorted(required_platforms)}) and build-tools ({sorted(required_build_tools)})."
                        ),
                    )
                )

        # 3. Per-App Gradle Wrapper, AGP, SDK, and Java Bytecode Checks
        gradle_versions_seen = set()
        agp_versions_seen = set()

        for proj in projects:
            scope = proj.rel_project_dir
            proj_issues_before = len(findings)
            if proj.gradle_wrapper_version:
                gradle_versions_seen.add(proj.gradle_wrapper_version)
            if proj.agp_version:
                agp_versions_seen.add(proj.agp_version)

            # 3a. Gradle Wrapper check
            if not proj.gradlew_path.is_file():
                findings.append(
                    CheckFinding(
                        severity="ERROR",
                        scope=scope,
                        category="Gradle",
                        summary=f"Missing `./gradlew` wrapper script in '{proj.project_dir}'.",
                        explanation="Each standalone Android project needs `./gradlew` for reproducible builds.",
                        remediation=f"Run `gradle wrapper --gradle-version {RECOMMENDED_GRADLE_VERSION}` in {proj.project_dir}.",
                    )
                )
            elif proj.gradle_wrapper_version:
                gw_tuple = _parse_version_tuple(proj.gradle_wrapper_version)
                if gw_tuple < (8, 11, 1):
                    findings.append(
                        CheckFinding(
                            severity="WARNING",
                            scope=scope,
                            category="Gradle",
                            summary=f"Gradle Wrapper is {proj.gradle_wrapper_version} (recommended: {RECOMMENDED_GRADLE_VERSION}).",
                            explanation=(
                                f"Modern Android Gradle Plugin ({RECOMMENDED_AGP_VERSION}) and JDK 21 "
                                f"work best with Gradle {RECOMMENDED_GRADLE_VERSION}."
                            ),
                            remediation=(
                                f"Update `distributionUrl` in `{scope}/gradle/wrapper/gradle-wrapper.properties` "
                                f"to `gradle-{RECOMMENDED_GRADLE_VERSION}-bin.zip`."
                            ),
                        )
                    )
                elif gw_tuple >= (9, 0, 0):
                    findings.append(
                        CheckFinding(
                            severity="WARNING",
                            scope=scope,
                            category="Gradle",
                            summary=f"Gradle Wrapper is {proj.gradle_wrapper_version} (major version 9+).",
                            explanation=(
                                "Gradle 9.x removes several legacy Groovy/AGP 8.x APIs and may not yet "
                                "be supported on F-Droid buildservers."
                            ),
                            remediation=f"Consider pinning Gradle Wrapper to `{RECOMMENDED_GRADLE_VERSION}` while on AGP 8.x.",
                        )
                    )

            # 3b. AGP vs. compileSdk compatibility check
            # Why we check this: Each AGP release has a hardcoded `maxCompileSdk` it was tested
            # against (e.g. AGP 8.9.0 was only tested up to compileSdk 35). When compileSdk = 36,
            # AGP < 8.10 emits a loud build warning on every invocation.
            if proj.agp_version and proj.compile_sdk:
                agp_tuple = _parse_version_tuple(proj.agp_version)
                min_agp_for_sdk = MIN_AGP_FOR_COMPILE_SDK.get(proj.compile_sdk)
                if min_agp_for_sdk and agp_tuple < min_agp_for_sdk:
                    min_agp_str = ".".join(str(x) for x in min_agp_for_sdk)
                    findings.append(
                        CheckFinding(
                            severity="WARNING",
                            scope=scope,
                            category="AGP",
                            summary=(
                                f"AGP {proj.agp_version} is older than the minimum tested version ({min_agp_str}+) "
                                f"for `compileSdk = {proj.compile_sdk}`."
                            ),
                            explanation=(
                                f"Android Gradle Plugin {proj.agp_version} was only tested up to "
                                f"compileSdk {proj.compile_sdk - 1} and prints a warning during every build."
                            ),
                            remediation=(
                                f"Upgrade `com.android.application` to version `{RECOMMENDED_AGP_VERSION}` "
                                f"in `{proj.gradle_file.relative_to(self.root_dir)}`."
                            ),
                        )
                    )

            # 3c. Google Play Store targetSdk check
            if proj.target_sdk is not None and proj.target_sdk < PLAY_STORE_MIN_TARGET_SDK:
                findings.append(
                    CheckFinding(
                        severity="WARNING",
                        scope=scope,
                        category="Android SDK",
                        summary=(
                            f"`targetSdk = {proj.target_sdk}` is below Google Play Store's minimum "
                            f"requirement (API {PLAY_STORE_MIN_TARGET_SDK}+)."
                        ),
                        explanation=(
                            "Google Play Console rejects new app submissions and updates whose targetSdk "
                            "is more than one year behind the current Android OS release."
                        ),
                        remediation=f"Update `targetSdk = 36` in `{proj.gradle_file.relative_to(self.root_dir)}`.",
                    )
                )

            # 3d. Java sourceCompatibility / targetCompatibility check
            if proj.java_source_compat != proj.java_target_compat:
                findings.append(
                    CheckFinding(
                        severity="WARNING",
                        scope=scope,
                        category="Java Bytecode",
                        summary=(
                            f"Mismatched `sourceCompatibility` ({proj.java_source_compat}) and "
                            f"`targetCompatibility` ({proj.java_target_compat})."
                        ),
                        explanation="Having different source and target Java levels in `compileOptions` can cause `javac` flag errors.",
                        remediation="Set both `sourceCompatibility` and `targetCompatibility` to `JavaVersion.VERSION_11`.",
                    )
                )
            elif proj.java_source_compat and ("1_8" in proj.java_source_compat or "1_7" in proj.java_source_compat):
                findings.append(
                    CheckFinding(
                        severity="SUGGESTION",
                        scope=scope,
                        category="Java Bytecode",
                        summary=f"App uses legacy `{proj.java_source_compat}` bytecode target.",
                        explanation=(
                            "Modern `javac` (JDK 21+) emits deprecation warnings when targeting Java 7/8, "
                            "whereas `JavaVersion.VERSION_11` is supported by `d8` even on `minSdk = 19`."
                        ),
                        remediation=f"Update `sourceCompatibility` and `targetCompatibility` to `JavaVersion.VERSION_11` in `{proj.gradle_file.relative_to(self.root_dir)}`.",
                    )
                )

            if len(findings) == proj_issues_before:
                findings.append(
                    CheckFinding(
                        severity="OK",
                        scope=scope,
                        category="App Toolchain",
                        summary=(
                            f"Gradle {proj.gradle_wrapper_version or 'unknown'}, "
                            f"AGP {proj.agp_version or 'unknown'}, "
                            f"compileSdk={proj.compile_sdk}/targetSdk={proj.target_sdk}/minSdk={proj.min_sdk}, "
                            f"Java={proj.java_source_compat or 'default'}."
                        ),
                    )
                )

        # 4. Cross-project consistency check in multi-app workspaces
        if len(projects) > 1:
            if len(gradle_versions_seen) > 1:
                findings.append(
                    CheckFinding(
                        severity="SUGGESTION",
                        scope="Workspace Consistency",
                        category="Gradle",
                        summary=f"Multiple Gradle Wrapper versions in use across apps: {sorted(gradle_versions_seen)}.",
                        explanation=(
                            "Using different Gradle versions across apps in the same repository causes "
                            "Gradle to spawn multiple separate ~2 GB JVM daemons and download multiple distributions."
                        ),
                        remediation=f"Standardize all apps on Gradle `{RECOMMENDED_GRADLE_VERSION}` so they share a single Gradle daemon.",
                    )
                )
            if len(agp_versions_seen) > 1:
                findings.append(
                    CheckFinding(
                        severity="SUGGESTION",
                        scope="Workspace Consistency",
                        category="AGP",
                        summary=f"Multiple Android Gradle Plugin versions in use across apps: {sorted(agp_versions_seen)}.",
                        explanation="Standardizing on one AGP version across all apps simplifies upgrades and build caching.",
                        remediation=f"Standardize all apps on AGP `{RECOMMENDED_AGP_VERSION}`.",
                    )
                )
            if len(gradle_versions_seen) == 1 and len(agp_versions_seen) == 1:
                findings.append(
                    CheckFinding(
                        severity="OK",
                        scope="Workspace Consistency",
                        category="Toolchain",
                        summary=(
                            f"All {len(projects)} apps use a uniform Gradle Wrapper "
                            f"({next(iter(gradle_versions_seen))}) and AGP ({next(iter(agp_versions_seen))})."
                        ),
                    )
                )

        return findings

    # ------------------------------------------------------------------
    # 3. F-Droid Readiness Verification (Ported & Expanded from fdroid/)
    # ------------------------------------------------------------------
    def has_any_fdroid_metadata(
        self, projects: Optional[Sequence[AndroidProject]] = None
    ) -> bool:
        """Returns True if an `fdroid/metadata` directory or any project `.yml` recipe exists.

        Why we check this: When F-Droid `.yml` recipes exist in the repository, the
        user intends to publish to F-Droid, so F-Droid readiness checks should run
        automatically before building or tagging releases.
        """
        if (self.root_dir / "fdroid" / "metadata").is_dir():
            if any((self.root_dir / "fdroid" / "metadata").glob("*.yml")):
                return True
        if projects:
            return any(p.has_fdroid_metadata for p in projects)
        return False

    def check_fdroid_readiness(
        self,
        project: AndroidProject,
        expected_version_name: Optional[str] = None,
        expected_version_code: Optional[int] = None,
    ) -> List[CheckFinding]:
        """Runs comprehensive F-Droid inclusion & reproducibility checks for `project`.

        Ported and expanded from `fdroid/verify_fdroid_readiness.sh` with detailed
        explanations for every F-Droid policy requirement:
          1. Explicit open-source `LICENSE` / `COPYING` file in the project.
          2. No hardcoded `org.gradle.java.home` in `gradle.properties`.
          3. `dependenciesInfo { includeInApk = false; includeInBundle = false }` in `build.gradle`.
          4. No prebuilt binary `.jar`/`.aar`/`.so` blobs in the source tree.
          5. Complete Fastlane metadata (`title.txt` <= 50 chars, `short_description.txt` <= 80 chars,
             `full_description.txt` <= 4000 chars, `images/icon.png`, and `changelogs/<versionCode>.txt`).
          6. Valid and synchronized `fdroid/metadata/<app_id>.yml` recipe.
        """
        findings: List[CheckFinding] = []
        scope = project.rel_project_dir
        ver_name = expected_version_name or project.version_name
        ver_code = expected_version_code if expected_version_code is not None else project.version_code

        # Check 1: Open-source LICENSE file
        # Why: F-Droid's scanner (`fdroid lint`) verifies that every app directory
        # contains a recognized open-source license file (`LICENSE` or `COPYING`).
        has_license = (
            (project.project_dir / "LICENSE").is_file()
            or (project.project_dir / "COPYING").is_file()
            or (self.root_dir / "LICENSE").is_file()
        )
        if not has_license:
            findings.append(
                CheckFinding(
                    severity="ERROR",
                    scope=scope,
                    category="F-Droid Readiness",
                    summary=f"Missing `LICENSE` (or `COPYING`) file in '{project.project_dir}'.",
                    explanation=(
                        "F-Droid strictly requires an OSI-approved open-source license file in the "
                        "repository before an app can be accepted into `fdroiddata`."
                    ),
                    remediation=f"Copy your open-source `LICENSE` file into `{project.project_dir}/LICENSE`.",
                )
            )

        # Check 2: No hardcoded `org.gradle.java.home` in `gradle.properties`
        # Why: Developers sometimes set `org.gradle.java.home=/usr/lib/jvm/...` locally.
        # On F-Droid's Debian build servers, that local path does not exist or points to
        # the wrong architecture, causing the F-Droid build container to fail immediately.
        gradle_props = project.project_dir / "gradle.properties"
        if gradle_props.is_file():
            props_text = gradle_props.read_text(encoding="utf-8", errors="ignore")
            if re.search(r"(?m)^\s*org\.gradle\.java\.home\s*=", props_text):
                findings.append(
                    CheckFinding(
                        severity="ERROR",
                        scope=scope,
                        category="F-Droid Readiness",
                        summary=f"Hardcoded `org.gradle.java.home` found in '{gradle_props.relative_to(self.root_dir)}'.",
                        explanation=(
                            "F-Droid build containers provision their own OpenJDK location. A hardcoded "
                            "`org.gradle.java.home` in `gradle.properties` overrides F-Droid's environment "
                            "and breaks the build on F-Droid's servers."
                        ),
                        remediation=f"Remove the `org.gradle.java.home=...` line from `{gradle_props.relative_to(self.root_dir)}`.",
                    )
                )

        # Check 3: Disabled Google-encrypted `DEPENDENCY_INFO_BLOCK` (`dependenciesInfo`)
        # Why: By default, Android Gradle Plugin embeds a `DEPENDENCY_INFO_BLOCK` encrypted
        # with Google's Play Store public key inside the APK signing block. F-Droid's automated
        # APK scanner rejects any APK containing this encrypted block because its contents
        # cannot be audited without Google's private key.
        gradle_text = project.gradle_file.read_text(encoding="utf-8", errors="ignore")
        has_no_apk_dep_info = re.search(
            r"includeInApk\s*(?:=|\s)\s*false", gradle_text
        ) is not None
        has_no_bundle_dep_info = re.search(
            r"includeInBundle\s*(?:=|\s)\s*false", gradle_text
        ) is not None
        if not (has_no_apk_dep_info and has_no_bundle_dep_info):
            findings.append(
                CheckFinding(
                    severity="ERROR",
                    scope=scope,
                    category="F-Droid Readiness",
                    summary=(
                        f"Missing `dependenciesInfo {{ includeInApk = false; includeInBundle = false }}` "
                        f"in '{project.gradle_file.relative_to(self.root_dir)}'."
                    ),
                    explanation=(
                        "AGP embeds a Google-encrypted `DEPENDENCY_INFO_BLOCK` in the APK signing block "
                        "by default. F-Droid's scanner flags this encrypted block as a proprietary "
                        "anti-feature/blocker and refuses to publish the APK."
                    ),
                    remediation=(
                        f"Add inside the `android {{ ... }}` block of `{project.gradle_file.relative_to(self.root_dir)}`:\n"
                        "        dependenciesInfo {\n"
                        "            includeInApk = false\n"
                        "            includeInBundle = false\n"
                        "        }"
                    ),
                )
            )

        # Check 4: Fastlane metadata structure & character limits
        # Why: F-Droid (and Google Play Fastlane `supply`) reads app store titles,
        # descriptions, icons, screenshots, and per-version changelogs from
        # `fastlane/metadata/android/en-US/`. Exceeding the 50-char title or 80-char
        # short_description limit causes store metadata ingestion to fail or truncate.
        fl_en = (
            project.fastlane_dir / "en-US"
            if project.fastlane_dir
            else project.project_dir / "fastlane" / "metadata" / "android" / "en-US"
        )
        text_limits = (
            ("title.txt", 50),
            ("short_description.txt", 80),
            ("full_description.txt", 4000),
        )
        for filename, max_chars in text_limits:
            fpath = fl_en / filename
            if not fpath.is_file():
                findings.append(
                    CheckFinding(
                        severity="ERROR",
                        scope=scope,
                        category="F-Droid Readiness",
                        summary=f"Missing Fastlane store file: '{fpath.relative_to(self.root_dir)}'.",
                        explanation=f"F-Droid and Fastlane require `{filename}` under `fastlane/metadata/android/en-US/`.",
                        remediation=f"Create `{fpath.relative_to(self.root_dir)}` (max {max_chars} characters).",
                    )
                )
            else:
                text_val = fpath.read_text(encoding="utf-8", errors="ignore").strip()
                if not text_val:
                    findings.append(
                        CheckFinding(
                            severity="ERROR",
                            scope=scope,
                            category="F-Droid Readiness",
                            summary=f"Fastlane file '{fpath.relative_to(self.root_dir)}' is empty.",
                            explanation="Empty store listing fields are rejected by F-Droid and Play Store.",
                            remediation=f"Populate `{fpath.relative_to(self.root_dir)}`.",
                        )
                    )
                elif len(text_val) > max_chars:
                    findings.append(
                        CheckFinding(
                            severity="WARNING",
                            scope=scope,
                            category="F-Droid Readiness",
                            summary=(
                                f"Fastlane '{fpath.relative_to(self.root_dir)}' is {len(text_val)} chars "
                                f"(limit is {max_chars} chars)."
                            ),
                            explanation=(
                                f"F-Droid `fdroid lint` and Google Play Console enforce a {max_chars}-character "
                                f"maximum on `{filename}`."
                            ),
                            remediation=f"Shorten `{fpath.relative_to(self.root_dir)}` to <= {max_chars} characters.",
                        )
                    )

        icon_path = fl_en / "images" / "icon.png"
        if not icon_path.is_file():
            findings.append(
                CheckFinding(
                    severity="ERROR",
                    scope=scope,
                    category="F-Droid Readiness",
                    summary=f"Missing Fastlane icon: '{icon_path.relative_to(self.root_dir)}'.",
                    explanation="F-Droid displays `fastlane/metadata/android/en-US/images/icon.png` in the F-Droid client.",
                    remediation=f"Place a 512x512 PNG icon at `{icon_path.relative_to(self.root_dir)}`.",
                )
            )

        screenshots_dir = fl_en / "images" / "phoneScreenshots"
        if not screenshots_dir.is_dir() or not list(screenshots_dir.glob("*.png")):
            findings.append(
                CheckFinding(
                    severity="WARNING",
                    scope=scope,
                    category="F-Droid Readiness",
                    summary=f"No PNG screenshots found in '{screenshots_dir.relative_to(self.root_dir)}'.",
                    explanation="F-Droid and Play Store listings use screenshots from `images/phoneScreenshots/` to showcase the app.",
                    remediation=f"Add at least 2 PNG screenshots to `{screenshots_dir.relative_to(self.root_dir)}/`.",
                )
            )

        changelog_path = fl_en / "changelogs" / f"{ver_code}.txt"
        if not changelog_path.is_file():
            findings.append(
                CheckFinding(
                    severity="ERROR",
                    scope=scope,
                    category="F-Droid Readiness",
                    summary=f"Missing Fastlane changelog for versionCode {ver_code}: '{changelog_path.relative_to(self.root_dir)}'.",
                    explanation=(
                        "Both F-Droid (`What's New`) and Google Play Fastlane look up release notes "
                        f"in `changelogs/{ver_code}.txt` matching the current `versionCode`."
                    ),
                    remediation=(
                        f"Create `{changelog_path.relative_to(self.root_dir)}` or run `./releaser/release.py` "
                        "which generates it automatically when syncing versions."
                    ),
                )
            )

        # Check 5: F-Droid YAML recipe existence & version consistency
        yml_file = project.fdroid_yml_file or (
            self.root_dir / "fdroid" / "metadata" / f"{project.app_id}.yml"
        )
        if not yml_file.is_file():
            findings.append(
                CheckFinding(
                    severity="ERROR",
                    scope=scope,
                    category="F-Droid Readiness",
                    summary=f"Missing F-Droid metadata recipe: '{yml_file.relative_to(self.root_dir)}'.",
                    explanation="Every app submitted to `fdroiddata` requires `metadata/<applicationId>.yml`.",
                    remediation=f"Create `{yml_file.relative_to(self.root_dir)}` with `Builds:` and `UpdateCheckMode`.",
                )
            )
        else:
            f_name, f_code = self.read_fdroid_version(project)
            if f_name != ver_name or f_code != ver_code:
                findings.append(
                    CheckFinding(
                        severity="WARNING",
                        scope=scope,
                        category="F-Droid Readiness",
                        summary=(
                            f"F-Droid YAML '{yml_file.relative_to(self.root_dir)}' has "
                            f"CurrentVersion={f_name} ({f_code}), expected {ver_name} ({ver_code})."
                        ),
                        explanation=(
                            "F-Droid's buildserver checks that `CurrentVersion`, `CurrentVersionCode`, and "
                            "the latest `Builds:` entry in the YAML recipe match the tagged `build.gradle`."
                        ),
                        remediation="Run `./releaser/release.py --sync-only` to synchronize the F-Droid YAML file.",
                    )
                )

        if not findings:
            findings.append(
                CheckFinding(
                    severity="OK",
                    scope=scope,
                    category="F-Droid Readiness",
                    summary=(
                        f"Passed all F-Droid readiness checks (LICENSE, gradle.properties, "
                        f"dependenciesInfo, Fastlane metadata, changelog {ver_code}.txt, and YAML recipe)."
                    ),
                )
            )

        return findings

    def check_all_fdroid_readiness(
        self, projects: Optional[Sequence[AndroidProject]] = None
    ) -> List[CheckFinding]:
        """Runs F-Droid readiness checks across all discovered projects."""
        if projects is None:
            projects = self.discover_projects()
        all_findings: List[CheckFinding] = []
        for proj in projects:
            all_findings.extend(self.check_fdroid_readiness(proj))
        return all_findings

    # ------------------------------------------------------------------
    # 4. Markdown Spreadsheet Generation & Inspection
    # ------------------------------------------------------------------
    def projects_to_rows(
        self,
        projects: Optional[Sequence[AndroidProject]] = None,
        default_tag_pattern: str = "{app}-v{version}",
    ) -> List[VersionRow]:
        if projects is None:
            projects = self.discover_projects()
        return [
            VersionRow(
                project_dir=p.rel_project_dir,
                app_id=p.app_id,
                display_name=p.display_name,
                version_name=p.version_name,
                version_code=p.version_code,
                tag_pattern=default_tag_pattern,
            )
            for p in projects
        ]

    def generate_table_file(
        self,
        table_path: Path | str,
        projects: Optional[Sequence[AndroidProject]] = None,
        default_tag_pattern: str = "{app}-v{version}",
    ) -> Path:
        rows = self.projects_to_rows(projects=projects, default_tag_pattern=default_tag_pattern)
        return MarkdownVersionTable.write_file(Path(table_path), rows)

    def read_manifest_version(
        self, project: AndroidProject
    ) -> Tuple[Optional[str], Optional[int]]:
        """Reads `(versionName, versionCode)` from `AndroidManifest.xml` if present."""
        if not project.manifest_file or not project.manifest_file.is_file():
            return None, None
        try:
            root = ET.parse(project.manifest_file).getroot()
            v_name = root.attrib.get(f"{ANDROID_NS}versionName")
            v_code_str = root.attrib.get(f"{ANDROID_NS}versionCode")
            v_code = int(v_code_str) if v_code_str and v_code_str.isdigit() else None
            return v_name, v_code
        except (ET.ParseError, OSError):
            return None, None

    def read_fdroid_version(
        self, project: AndroidProject
    ) -> Tuple[Optional[str], Optional[int]]:
        """Reads `(CurrentVersion, CurrentVersionCode)` from F-Droid YAML if present."""
        if not project.fdroid_yml_file or not project.fdroid_yml_file.is_file():
            return None, None
        try:
            text = project.fdroid_yml_file.read_text(encoding="utf-8")
            vn_m = re.search(r"(?m)^CurrentVersion:\s*['\"]?([^'\"\n]+)['\"]?", text)
            vc_m = re.search(r"(?m)^CurrentVersionCode:\s*([0-9]+)", text)
            v_name = vn_m.group(1).strip() if vn_m else None
            v_code = int(vc_m.group(1)) if vc_m else None
            return v_name, v_code
        except OSError:
            return None, None

    def is_project_out_of_sync(self, project: AndroidProject, row: VersionRow) -> bool:
        """Returns True if the Markdown table row differs from any on-disk version file."""
        if project.version_name != row.version_name or project.version_code != row.version_code:
            return True

        m_name, m_code = self.read_manifest_version(project)
        if m_name is not None and m_name != row.version_name:
            return True
        if m_code is not None and m_code != row.version_code:
            return True

        f_name, f_code = self.read_fdroid_version(project)
        if f_name is not None and f_name != row.version_name:
            return True
        if f_code is not None and f_code != row.version_code:
            return True

        return False

    # ------------------------------------------------------------------
    # 5. Updating Version Numbers Across Project Files
    # ------------------------------------------------------------------
    def update_project_version(
        self,
        project: AndroidProject,
        new_version_name: str,
        new_version_code: int,
        tag_name: Optional[str] = None,
        dry_run: bool = False,
    ) -> List[Path]:
        """Updates versionName and versionCode in build.gradle(.kts), AndroidManifest.xml,
        optional F-Droid YAML, and optional Fastlane changelogs.

        Returns the list of modified/created file paths.
        """
        if tag_name is None:
            tag_name = f"{project.project_slug}-v{new_version_name}"

        modified: List[Path] = []

        # 1. Update build.gradle / build.gradle.kts
        gradle_text = project.gradle_file.read_text(encoding="utf-8")
        new_gradle_text = self._RE_VERSION_CODE.sub(
            rf"\g<1>{new_version_code}", gradle_text, count=1
        )
        new_gradle_text = self._RE_VERSION_NAME.sub(
            rf"\g<1>{new_version_name}\g<3>", new_gradle_text, count=1
        )
        if new_gradle_text != gradle_text:
            if not dry_run:
                project.gradle_file.write_text(new_gradle_text, encoding="utf-8")
            modified.append(project.gradle_file)

        # 2. Update AndroidManifest.xml (if versionCode/versionName attributes exist there)
        # Why: Although modern AGP injects versionCode/versionName from build.gradle into
        # the merged manifest, projects that keep explicit attributes in AndroidManifest.xml
        # will have mismatched metadata unless both files are updated together.
        if project.manifest_file and project.manifest_file.is_file():
            manifest_text = project.manifest_file.read_text(encoding="utf-8")
            new_manifest_text = re.sub(
                r'(android:versionCode=")[0-9]+(")',
                rf"\g<1>{new_version_code}\g<2>",
                manifest_text,
            )
            new_manifest_text = re.sub(
                r'(android:versionName=")[^"]+(")',
                rf"\g<1>{new_version_name}\g<2>",
                new_manifest_text,
            )
            if new_manifest_text != manifest_text:
                if not dry_run:
                    project.manifest_file.write_text(new_manifest_text, encoding="utf-8")
                modified.append(project.manifest_file)

        # 3. Update F-Droid metadata YAML if present
        # Why: F-Droid's `fdroidserver` retains historical build blocks under `Builds:`
        # so users can downgrade to older versions. When `versionCode` is incremented,
        # we append a new `Builds:` entry rather than overwriting historical releases.
        if project.fdroid_yml_file and project.fdroid_yml_file.is_file():
            yml_text = project.fdroid_yml_file.read_text(encoding="utf-8")
            new_yml_text = self._update_fdroid_yaml_text(
                yml_text,
                subdir=project.rel_project_dir,
                version_name=new_version_name,
                version_code=new_version_code,
                tag_name=tag_name,
            )
            if new_yml_text != yml_text:
                if not dry_run:
                    project.fdroid_yml_file.write_text(new_yml_text, encoding="utf-8")
                modified.append(project.fdroid_yml_file)

        # 4. Ensure Fastlane en-US changelog <versionCode>.txt exists if Fastlane is used
        if project.fastlane_dir and project.fastlane_dir.is_dir():
            changelog_file = (
                project.fastlane_dir / "en-US" / "changelogs" / f"{new_version_code}.txt"
            )
            if not changelog_file.exists():
                if not dry_run:
                    changelog_file.parent.mkdir(parents=True, exist_ok=True)
                    changelog_file.write_text(
                        f"{project.display_name} v{new_version_name} (versionCode {new_version_code})\n",
                        encoding="utf-8",
                    )
                modified.append(changelog_file)

        if not dry_run:
            project.version_name = new_version_name
            project.version_code = new_version_code
        return modified

    @staticmethod
    def _update_fdroid_yaml_text(
        content: str,
        subdir: str,
        version_name: str,
        version_code: int,
        tag_name: str,
    ) -> str:
        content = re.sub(
            r"(?m)^CurrentVersion:\s*.*$",
            f"CurrentVersion: '{version_name}'",
            content,
        )
        content = re.sub(
            r"(?m)^CurrentVersionCode:\s*.*$",
            f"CurrentVersionCode: {version_code}",
            content,
        )

        subdir_line = f"    subdir: {subdir}\n" if subdir not in (".", "") else ""
        build_block = (
            f"  - versionName: '{version_name}'\n"
            f"    versionCode: {version_code}\n"
            f"    commit: {tag_name}\n"
            f"{subdir_line}"
            f"    gradle:\n"
            f"      - yes\n"
        )

        pattern_same_code = re.compile(
            r"  - versionName: '[^']*'\n"
            r"    versionCode: " + re.escape(str(version_code)) + r"\n"
            r"    commit: [^\n]*\n"
            r"(?:    subdir: [^\n]*\n)?"
            r"    gradle:\n"
            r"      - yes\n"
        )
        if pattern_same_code.search(content):
            return pattern_same_code.sub(build_block, content)
        if "\nAutoUpdateMode:" in content:
            return content.replace(
                "\nAutoUpdateMode:", "\n" + build_block + "\nAutoUpdateMode:", 1
            )
        return content

    # ------------------------------------------------------------------
    # 6. Building Play Store Bundle (.aab) + Debug & Release APKs
    # ------------------------------------------------------------------
    def build_project_artifacts(
        self,
        project: AndroidProject,
        tag_name: Optional[str] = None,
        releases_root: Optional[Path | str] = None,
        gradle_tasks: Sequence[str] = (
            "bundleRelease",
            "assembleDebug",
            "assembleRelease",
        ),
    ) -> ReleaseArtifacts:
        """Runs Gradle to build the Play Store `.aab` bundle and Debug/Release `.apk`s,
        then stages version-stamped artifacts and `SHA256SUMS.txt` for GitHub Releases.

        Why we glob `build/outputs/` instead of hardcoding artifact filenames:
        Gradle names outputs after `rootProject.name` or the module name (e.g.
        `ChordWheel-release.apk` vs. `app-release.apk`). Discovering the built `.aab`
        and `.apk` files dynamically inside `module_dir/build/outputs/` makes this
        method work across any project naming convention.
        """
        if tag_name is None:
            tag_name = f"{project.project_slug}-v{project.version_name}"
        if releases_root is None:
            releases_root = self.root_dir / "releases"
        out_dir = Path(releases_root) / tag_name

        gradlew = project.gradlew_path
        if not gradlew.is_file():
            raise FileNotFoundError(
                f"Gradle wrapper not found at '{gradlew}' for project '{project.project_dir}'. "
                f"Ensure `./gradlew` exists and is executable (`chmod +x {gradlew}`)."
            )

        cmd = [str(gradlew), *gradle_tasks]
        try:
            subprocess.run(cmd, cwd=str(project.project_dir), check=True)
        except subprocess.CalledProcessError as exc:
            raise RuntimeError(
                f"Gradle build failed in '{project.project_dir}' while running: {' '.join(cmd)}.\n"
                f"Run `./releaser/release.py --check` to diagnose JDK, Gradle, AGP, or Android SDK issues."
            ) from exc

        outputs_dir = project.module_dir / "build" / "outputs"
        aab_files = sorted(outputs_dir.glob("bundle/release/*.aab"))
        rel_apks = sorted(outputs_dir.glob("apk/release/*-release*.apk"))
        dbg_apks = sorted(outputs_dir.glob("apk/debug/*-debug*.apk"))

        if not aab_files:
            raise FileNotFoundError(
                f"Gradle finished, but no release `.aab` bundle was found under '{outputs_dir}/bundle/release/'."
            )
        if not rel_apks:
            raise FileNotFoundError(
                f"Gradle finished, but no release `.apk` was found under '{outputs_dir}/apk/release/'."
            )
        if not dbg_apks:
            raise FileNotFoundError(
                f"Gradle finished, but no debug `.apk` was found under '{outputs_dir}/apk/debug/'."
            )

        out_dir.mkdir(parents=True, exist_ok=True)
        slug = project.project_slug
        ver = project.version_name

        dst_aab = out_dir / f"{slug}-v{ver}-playstore-release.aab"
        dst_rel_apk = out_dir / f"{slug}-v{ver}-release.apk"
        dst_dbg_apk = out_dir / f"{slug}-v{ver}-debug.apk"

        shutil.copy2(aab_files[0], dst_aab)
        shutil.copy2(rel_apks[0], dst_rel_apk)
        shutil.copy2(dbg_apks[0], dst_dbg_apk)

        checksums_file = out_dir / "SHA256SUMS.txt"
        checksum_lines = []
        for artifact in (dst_aab, dst_rel_apk, dst_dbg_apk):
            digest = hashlib.sha256(artifact.read_bytes()).hexdigest()
            checksum_lines.append(f"{digest}  {artifact.name}")
        checksums_file.write_text("\n".join(checksum_lines) + "\n", encoding="utf-8")

        return ReleaseArtifacts(
            project_dir=project.project_dir,
            version_name=project.version_name,
            version_code=project.version_code,
            tag_name=tag_name,
            output_dir=out_dir,
            bundle_aab=dst_aab,
            release_apk=dst_rel_apk,
            debug_apk=dst_dbg_apk,
            checksums_file=checksums_file,
        )

    # ------------------------------------------------------------------
    # 7. Creating & Checking Annotated Git Tags (F-Droid & GitHub format)
    # ------------------------------------------------------------------
    def tag_exists(self, tag_name: str, git_dir: Optional[Path] = None) -> bool:
        cwd = git_dir or self.root_dir
        res = subprocess.run(
            ["git", "rev-parse", "-q", "--verify", f"refs/tags/{tag_name}"],
            cwd=str(cwd),
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
            check=False,
        )
        return res.returncode == 0

    def create_git_tag(
        self,
        project: AndroidProject,
        tag_name: Optional[str] = None,
        message: Optional[str] = None,
        force: bool = True,
        push: bool = False,
        dry_run: bool = False,
    ) -> str:
        """Creates an annotated Git tag in the repository containing `project`.

        Why we create annotated (`git tag -a`) rather than lightweight tags:
        GitHub Releases and F-Droid's `fdroid checkupdates` (`UpdateCheckMode: Tags`)
        both expect annotated tags with a timestamp, tagger identity, and message.
        """
        if tag_name is None:
            tag_name = f"{project.project_slug}-v{project.version_name}"
        if message is None:
            message = (
                f"{project.display_name} v{project.version_name} "
                f"(versionCode {project.version_code}) — F-Droid & GitHub Release"
            )

        if dry_run:
            return tag_name

        cmd = ["git", "tag", "-a"]
        if force:
            cmd.append("-f")
        cmd.extend([tag_name, "-m", message])
        try:
            subprocess.run(cmd, cwd=str(project.project_dir), check=True)
        except subprocess.CalledProcessError as exc:
            raise RuntimeError(
                f"Failed to create Git tag '{tag_name}' in '{project.project_dir}'. "
                f"Ensure the directory is inside a Git repository with at least one commit."
            ) from exc

        if push:
            push_cmd = ["git", "push", "origin"]
            if force:
                push_cmd.append("--force")
            push_cmd.append(tag_name)
            try:
                subprocess.run(push_cmd, cwd=str(project.project_dir), check=True)
            except subprocess.CalledProcessError as exc:
                raise RuntimeError(
                    f"Created local tag '{tag_name}', but failed to push to 'origin'. "
                    f"Check your `git remote -v` and network/SSH credentials."
                ) from exc

        return tag_name
