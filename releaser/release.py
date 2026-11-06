#!/usr/bin/env python3
"""Reads a Markdown version table, verifies toolchain & F-Droid readiness, and releases Android apps.

Why this script exists:
  Releasing Android applications across both Google Play and F-Droid requires
  keeping several files and metadata trees in lockstep (`build.gradle`,
  `AndroidManifest.xml`, Fastlane changelogs, F-Droid `.yml` recipes, and Git
  tags), while also ensuring the host JDK, Gradle Wrapper, Android Gradle Plugin,
  and SDK levels are mutually compatible.

  This runner separates *policy* (which versions to release, stored in a
  human-editable Markdown table `releaser/versions.md`) from *mechanism*
  (validating the environment, syncing files, building `.aab`/`.apk` artifacts,
  and tagging).

Workflow for users:
  1. Generate or refresh the Markdown version table:
       ./releaser/scan_versions.py
  2. Audit the build environment and F-Droid readiness at any time:
       ./releaser/release.py --check
  3. Edit the 'Version Name' and 'Version Code' columns in `releaser/versions.md`
     for any app(s) you want to increment (columns can be in any order).
  4. Run this release runner:
       ./releaser/release.py
"""

from __future__ import annotations

import argparse
from pathlib import Path
import sys
from typing import Dict, List, Sequence

# Why we insert `SCRIPT_DIR.parent` into `sys.path`:
# Allows running `./releaser/release.py` directly from any working directory
# without requiring a prior `pip install` into the active Python environment.
SCRIPT_DIR = Path(__file__).resolve().parent
if str(SCRIPT_DIR.parent) not in sys.path:
    sys.path.insert(0, str(SCRIPT_DIR.parent))

from releaser.core import (
    AndroidProject,
    AndroidReleaser,
    CheckFinding,
    MarkdownVersionTable,
    VersionRow,
)


def resolve_default_root() -> Path:
    """Returns the target root directory when `--root` is omitted.

    If the current working directory contains Android projects directly (e.g.
    `build.gradle*` or `*/build.gradle*`), uses `cwd`; otherwise defaults to the
    parent repository of `releaser/` (`SCRIPT_DIR.parent`).
    """
    cwd = Path.cwd().resolve()
    repo_root = SCRIPT_DIR.parent
    if cwd == SCRIPT_DIR or cwd == repo_root:
        return repo_root
    if any(cwd.glob("build.gradle*")) or any(cwd.glob("*/build.gradle*")) or any(cwd.glob("*/*/build.gradle*")):
        return cwd
    return repo_root


def print_findings_report(
    title: str,
    findings: Sequence[CheckFinding],
    show_ok: bool = False,
) -> tuple[int, int, int]:
    """Prints a human-understandable diagnostic report and returns `(errors, warnings, suggestions)`.

    Why we separate `ERROR`, `WARNING`, `SUGGESTION`, and `OK`:
      - `ERROR` indicates a condition that will break the Gradle build, fail
        F-Droid `fdroidserver` verification, or reject a Play Store upload.
      - `WARNING` indicates a configuration that currently works (often due to
        a local fallback) but is fragile or nearing deprecation.
      - `SUGGESTION` highlights concrete upgrades (e.g. bumping Gradle or Java
        language level) that improve build speed or maintainability.
      - `OK` confirms what was verified when running `--check` / `--verbose`.
    """
    errors = [f for f in findings if f.severity == "ERROR"]
    warnings = [f for f in findings if f.severity == "WARNING"]
    suggestions = [f for f in findings if f.severity == "SUGGESTION"]
    oks = [f for f in findings if f.severity == "OK"]

    actionable = errors + warnings + suggestions
    if not actionable and not show_ok:
        return (0, 0, 0)

    print("=" * 78)
    print(title)
    print("=" * 78)

    if show_ok and oks:
        for f in oks:
            print(f"  {f.format_verbose()}")
        if actionable:
            print("-" * 78)

    for f in actionable:
        print(f.format_verbose())
        print("")

    print(
        f"Summary: {len(errors)} error(s), {len(warnings)} warning(s), "
        f"{len(suggestions)} suggestion(s), {len(oks)} passed check(s)."
    )
    print("=" * 78)
    print("")
    return (len(errors), len(warnings), len(suggestions))


def print_status_comparison(
    releaser: AndroidReleaser,
    rows: List[VersionRow],
    projects_by_dir: Dict[str, AndroidProject],
    projects_by_id: Dict[str, AndroidProject],
) -> None:
    """Prints a terminal table comparing `versions.md` rows against on-disk project files.

    Why this view is useful:
      Before running a lengthy multi-app Gradle build, the developer can see at
      a glance which apps have uncommitted version bumps in `versions.md`
      (`MODIFIED`), whether the corresponding Git tag already exists, whether
      F-Droid YAML metadata is present, and whether the release `.aab`/`.apk`
      bundles have already been built.
    """
    headers = (
        "Project Dir",
        "App ID",
        "Table Ver (Code)",
        "Disk Ver (Code)",
        "F-Droid",
        "Git Tag",
        "Artifacts",
        "State",
    )
    table_rows = []
    for row in rows:
        proj = projects_by_dir.get(row.project_dir) or projects_by_id.get(row.app_id)
        tag_name = row.render_tag()
        table_ver = f"{row.version_name} ({row.version_code})"
        if proj is None:
            table_rows.append(
                (row.project_dir, row.app_id, table_ver, "NOT FOUND", "-", tag_name, "-", "MISSING")
            )
            continue

        disk_ver = f"{proj.version_name} ({proj.version_code})"
        fdroid_status = "yes" if proj.has_fdroid_metadata else "no"
        tag_ok = "ok" if releaser.tag_exists(tag_name, proj.project_dir) else "missing"
        out_dir = releaser.root_dir / "releases" / tag_name
        slug = proj.project_slug
        has_artifacts = (
            (out_dir / f"{slug}-v{row.version_name}-playstore-release.aab").is_file()
            and (out_dir / f"{slug}-v{row.version_name}-release.apk").is_file()
            and (out_dir / f"{slug}-v{row.version_name}-debug.apk").is_file()
        )
        art_status = "ready" if has_artifacts else "not built"
        state = "MODIFIED" if releaser.is_project_out_of_sync(proj, row) else "in sync"
        table_rows.append(
            (
                row.project_dir,
                row.app_id,
                table_ver,
                disk_ver,
                fdroid_status,
                f"{tag_name} ({tag_ok})",
                art_status,
                state,
            )
        )

    widths = [len(h) for h in headers]
    for r in table_rows:
        for i, cell in enumerate(r):
            widths[i] = max(widths[i], len(cell))

    def fmt(cells: tuple[str, ...]) -> str:
        return "| " + " | ".join(f"{cells[i]:<{widths[i]}}" for i in range(len(headers))) + " |"

    print(fmt(headers))
    print("| " + " | ".join("-" * w for w in widths) + " |")
    for r in table_rows:
        print(fmt(r))


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(
        description=(
            "Read a Markdown version table, check build environment & F-Droid readiness, "
            "and update versions, build Play Store bundles & APKs, and create Git tags."
        )
    )
    parser.add_argument(
        "apps",
        nargs="*",
        help="Optional project directories or app IDs to force-release (default: auto-detect modified rows in table).",
    )
    parser.add_argument(
        "--root",
        "-r",
        type=Path,
        default=None,
        help="Top-level directory containing Android projects (default: current directory or repo root).",
    )
    parser.add_argument(
        "--table",
        "-t",
        type=Path,
        default=SCRIPT_DIR / "versions.md",
        help="Path to the Markdown version table (default: releaser/versions.md).",
    )
    parser.add_argument(
        "--check",
        "--doctor",
        action="store_true",
        help=(
            "Audit the build environment (Java/JDK, Gradle, AGP, Android SDK) and "
            "F-Droid readiness (if any .yml files exist), print verbose suggestions, and exit."
        ),
    )
    parser.add_argument(
        "--scan",
        "--init",
        action="store_true",
        help="Scan `--root` recursively and regenerate `--table` from on-disk project files, then exit.",
    )
    parser.add_argument(
        "--status",
        "-l",
        action="store_true",
        help="Show a comparison table between the Markdown file and on-disk projects, then exit.",
    )
    parser.add_argument(
        "--all",
        "-a",
        action="store_true",
        help="Run the release pipeline for every app in the table, even if its version did not change.",
    )
    parser.add_argument(
        "--sync-only",
        "-s",
        action="store_true",
        help="Only update version numbers and Git tags without running Gradle builds.",
    )
    parser.add_argument(
        "--no-tag",
        action="store_true",
        help="Skip creating/updating Git tags.",
    )
    parser.add_argument(
        "--push",
        "-p",
        action="store_true",
        help="Push created/updated Git tags to 'origin'.",
    )
    parser.add_argument(
        "--skip-checks",
        action="store_true",
        help="Skip pre-flight build environment and F-Droid readiness checks before building.",
    )
    parser.add_argument(
        "--verbose",
        "-v",
        action="store_true",
        help="Print passing [OK] check items in addition to warnings, suggestions, and errors.",
    )
    parser.add_argument(
        "--dry-run",
        "-n",
        action="store_true",
        help="Show what would be updated, built, and tagged without modifying files or running Gradle.",
    )
    args = parser.parse_args(argv)

    root_dir = (args.root or resolve_default_root()).resolve()
    table_path = args.table.resolve()
    releaser = AndroidReleaser(root_dir=root_dir)
    projects = releaser.discover_projects()

    if not projects:
        print(
            f"[ERROR] No Android projects discovered under: {root_dir}\n"
            f"  Why this matters:\n"
            f"    `AndroidReleaser` searches recursively for `build.gradle` or `build.gradle.kts`\n"
            f"    files that apply `com.android.application` and define `versionName`/`versionCode`.\n"
            f"  Suggested fix:\n"
            f"    Run from your Android repository root or pass `--root /path/to/projects`.",
            file=sys.stderr,
        )
        return 1

    # Mode 1: `--scan` / `--init` regenerates `versions.md` from disk.
    if args.scan:
        releaser.generate_table_file(table_path, projects=projects)
        print(MarkdownVersionTable.dumps(releaser.projects_to_rows(projects)), end="")
        print(f"\n[OK] Populated Markdown version table at: {table_path}")
        return 0

    # Mode 2: `--check` / `--doctor` runs a full diagnostic of the build
    # environment (Gradle, AGP, JDK, SDK) and F-Droid readiness (whenever any
    # F-Droid `.yml` files exist in the repository).
    if args.check:
        env_findings = releaser.check_build_environment(projects)
        err_cnt, _, _ = print_findings_report(
            "Build Environment & Toolchain Audit (Java, Gradle, AGP, Android SDK)",
            env_findings,
            show_ok=True,
        )

        # Why we automatically check F-Droid readiness whenever `.yml` files exist:
        # If the repository maintains F-Droid `.yml` recipes, every release is
        # expected to remain F-Droid compliant without the developer having to
        # remember a separate shell script.
        if releaser.has_any_fdroid_metadata(projects):
            fdroid_findings = releaser.check_all_fdroid_readiness(projects)
            fd_err, _, _ = print_findings_report(
                "F-Droid Readiness Audit (Detected F-Droid .yml Metadata)",
                fdroid_findings,
                show_ok=True,
            )
            err_cnt += fd_err
        return 1 if err_cnt > 0 else 0

    if not table_path.is_file():
        print(f"Table '{table_path}' not found; scanning '{root_dir}' to create it...")
        releaser.generate_table_file(table_path, projects=projects)

    try:
        rows = MarkdownVersionTable.read_file(table_path)
    except ValueError as exc:
        print(f"[ERROR] Could not parse Markdown version table:\n  {exc}", file=sys.stderr)
        return 1

    projects_by_dir: Dict[str, AndroidProject] = {p.rel_project_dir: p for p in projects}
    projects_by_id: Dict[str, AndroidProject] = {p.app_id: p for p in projects}

    # Mode 3: `--status` prints the comparison table and any actionable environment
    # or F-Droid warnings/errors.
    if args.status:
        print_status_comparison(releaser, rows, projects_by_dir, projects_by_id)
        if not args.skip_checks:
            env_findings = releaser.check_build_environment(projects)
            fdroid_findings = (
                releaser.check_all_fdroid_readiness(projects)
                if releaser.has_any_fdroid_metadata(projects)
                else []
            )
            all_findings = env_findings + fdroid_findings
            if any(f.severity != "OK" for f in all_findings) or args.verbose:
                print("")
                print_findings_report(
                    "Environment & F-Droid Health Notices (run `--check` for full report)",
                    all_findings,
                    show_ok=args.verbose,
                )
        return 0

    # Determine which projects should be released in this invocation.
    requested_targets = {a.rstrip("/") for a in args.apps}
    selected: List[tuple[AndroidProject, VersionRow]] = []

    for row in rows:
        proj = projects_by_dir.get(row.project_dir) or projects_by_id.get(row.app_id)
        if proj is None:
            print(
                f"[WARNING] Project '{row.project_dir}' ({row.app_id}) listed in {table_path.name} "
                f"was not found under {root_dir}.\n"
                f"  Why this matters:\n"
                f"    The directory may have been renamed or moved since `{table_path.name}` was generated.\n"
                f"  Suggested fix:\n"
                f"    Run `./releaser/scan_versions.py` to refresh the table from disk.",
                file=sys.stderr,
            )
            continue

        if args.all:
            selected.append((proj, row))
        elif requested_targets:
            if (
                row.project_dir in requested_targets
                or row.app_id in requested_targets
                or proj.project_slug in requested_targets
            ):
                selected.append((proj, row))
        elif releaser.is_project_out_of_sync(proj, row):
            selected.append((proj, row))

    if not selected:
        print(f"All projects on disk match '{table_path.name}'. No version changes detected.\n")
        print_status_comparison(releaser, rows, projects_by_dir, projects_by_id)
        print(
            f"\nTip: Edit 'Version Name' and 'Version Code' in {table_path} and re-run,\n"
            f"     or pass an app name (`./releaser/release.py chordwheel`), `--all`, or `--check`."
        )
        return 0

    # Pre-flight Check 1: Validate the build environment for the selected projects
    # BEFORE mutating files or starting slow Gradle builds.
    selected_projects = [proj for proj, _ in selected]
    if not args.skip_checks:
        env_findings = releaser.check_build_environment(selected_projects)
        env_errors, _, _ = print_findings_report(
            "Pre-Flight Build Environment Check",
            env_findings,
            show_ok=args.verbose,
        )
        if env_errors > 0:
            print(
                "[ABORTED] Pre-flight build environment check found blocking error(s).\n"
                "          Fix the error(s) above or pass `--skip-checks` to force execution.",
                file=sys.stderr,
            )
            return 1

    # Check whether F-Droid `.yml` files exist anywhere in this repository.
    # Why: Per user requirement, if `.yml` files exist, F-Droid readiness must
    # always be verified automatically so no release accidentally breaks F-Droid.
    enforce_fdroid = releaser.has_any_fdroid_metadata(projects)

    created_tags: List[str] = []
    for proj, row in selected:
        tag_name = row.render_tag()
        print("=" * 78)
        print(
            f"Releasing {row.display_name} ({proj.rel_project_dir}) "
            f"-> v{row.version_name} (code {row.version_code}), tag: {tag_name}"
        )
        print("=" * 78)

        # Step 1: Synchronize versionName and versionCode across build.gradle,
        # AndroidManifest.xml, F-Droid YAML metadata, and Fastlane changelogs.
        # Why we do this BEFORE `check_fdroid_readiness`:
        # `update_project_version` updates the F-Droid `.yml` recipe and creates
        # the new Fastlane changelog `<versionCode>.txt`. Running the F-Droid
        # readiness check immediately afterward verifies that the newly updated
        # files are 100% compliant before spending time on the Gradle build.
        try:
            updated_files = releaser.update_project_version(
                proj,
                new_version_name=row.version_name,
                new_version_code=row.version_code,
                tag_name=tag_name,
                dry_run=args.dry_run,
            )
        except ValueError as exc:
            print(f"[ERROR] Failed to update version files for {proj.rel_project_dir}:\n{exc}", file=sys.stderr)
            return 1

        if updated_files:
            for uf in updated_files:
                rel_uf = uf.relative_to(root_dir) if uf.is_relative_to(root_dir) else uf
                prefix = "[DRY-RUN SYNC]" if args.dry_run else "[SYNC]"
                print(f"  {prefix} Updated {rel_uf}")
        else:
            print("  [SYNC] Version files already up to date.")

        # Step 2: If F-Droid `.yml` files exist in the repo, automatically verify
        # F-Droid readiness for this project prior to building & tagging.
        if enforce_fdroid and not args.skip_checks:
            # In dry-run mode, files were not written to disk, so check against
            # the on-disk version; in normal mode, `proj.version_name`/`version_code`
            # have already been updated to the new target version.
            fdroid_findings = releaser.check_fdroid_readiness(proj)
            fd_errors = [f for f in fdroid_findings if f.severity == "ERROR"]
            fd_non_ok = [f for f in fdroid_findings if f.severity != "OK"]
            if fd_non_ok or args.verbose:
                print_findings_report(
                    f"F-Droid Readiness Check: {proj.rel_project_dir}",
                    fdroid_findings,
                    show_ok=args.verbose,
                )
            else:
                print(f"  [FDROID] Verified F-Droid readiness ({len(fdroid_findings)} checks passed).")

            if fd_errors and not args.dry_run:
                print(
                    f"[ABORTED] F-Droid readiness check failed for '{proj.rel_project_dir}'.\n"
                    f"          Resolve the error(s) detailed above before building/tagging.",
                    file=sys.stderr,
                )
                return 1

        # Step 3: Run Gradle to build the Play Store `.aab` bundle, release `.apk`,
        # and debug `.apk`, and compute `SHA256SUMS.txt`.
        if not args.sync_only:
            if args.dry_run:
                print(
                    f"  [DRY-RUN BUILD] Would run Gradle bundleRelease, assembleDebug, assembleRelease in {proj.rel_project_dir}"
                )
            else:
                print(
                    f"  [BUILD] Running bundleRelease, assembleDebug, assembleRelease in {proj.rel_project_dir}..."
                )
                try:
                    artifacts = releaser.build_project_artifacts(proj, tag_name=tag_name)
                except (RuntimeError, FileNotFoundError) as exc:
                    print(f"\n{exc}", file=sys.stderr)
                    return 1

                print(f"  [OK]    Play Store Bundle : {artifacts.bundle_aab.relative_to(root_dir)}")
                print(f"  [OK]    GitHub Release APK: {artifacts.release_apk.relative_to(root_dir)}")
                print(f"  [OK]    GitHub Debug APK  : {artifacts.debug_apk.relative_to(root_dir)}")
                print(f"  [OK]    SHA-256 Checksums : {artifacts.checksums_file.relative_to(root_dir)}")

        # Step 4: Create (or update) the annotated Git tag required by F-Droid's
        # `UpdateCheckMode: Tags` and GitHub Releases.
        if not args.no_tag:
            try:
                releaser.create_git_tag(
                    proj,
                    tag_name=tag_name,
                    push=args.push,
                    dry_run=args.dry_run,
                )
            except RuntimeError as exc:
                print(f"\n{exc}", file=sys.stderr)
                return 1
            tag_prefix = "[DRY-RUN TAG]" if args.dry_run else "[TAG]"
            print(f"  {tag_prefix} {tag_name}")
            created_tags.append(tag_name)
        print("")

    if created_tags and not args.push:
        print("-" * 78)
        print(f"Created/updated Git tag(s): {' '.join(created_tags)}")
        print("To push tag(s) to GitHub (for F-Droid & GitHub Releases):")
        print(f"  git push origin {' '.join(created_tags)}")
        if not args.sync_only:
            print("To publish GitHub Release artifacts using the GitHub CLI ('gh'):")
            for t in created_tags:
                print(f'  gh release create {t} releases/{t}/* --title "{t}" --generate-notes')
        print("-" * 78)

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
