#!/usr/bin/env python3
"""Scans a directory tree recursively for Android projects and populates a Markdown version table.

Why a separate scanner script exists:
  When onboarding a new repository (or after someone manually edits a
  `build.gradle` file outside of the release tool), the user needs a way to
  bootstrap or refresh the human-editable version spreadsheet (`versions.md`)
  from the ground truth on disk without triggering any builds or Git tags.

Usage examples:
  # Scan the parent repository (or current directory) and write releaser/versions.md:
  ./releaser/scan_versions.py

  # Scan any arbitrary directory tree and write a custom Markdown table:
  ./releaser/scan_versions.py --root ~/projects/my-android-apps --table ~/projects/my-android-apps/versions.md
"""

from __future__ import annotations

import argparse
from pathlib import Path
import sys

# Why we insert `SCRIPT_DIR.parent` into `sys.path`:
# Users frequently invoke `./releaser/scan_versions.py` directly from the
# repository root or from inside `releaser/` without installing `releaser` into
# a virtualenv via `pip install -e .`. Adding the parent directory ensures
# `from releaser.core import ...` always resolves cleanly regardless of cwd.
SCRIPT_DIR = Path(__file__).resolve().parent
if str(SCRIPT_DIR.parent) not in sys.path:
    sys.path.insert(0, str(SCRIPT_DIR.parent))

from releaser.core import AndroidReleaser, MarkdownVersionTable


def resolve_default_root() -> Path:
    """Chooses the default root directory to scan when `--root` is omitted.

    Why this heuristic is used:
      If the user runs `./scan_versions.py` from a directory that contains Android
      projects (`build.gradle*` or `*/build.gradle*`), we scan `cwd`. If they run
      it while `cd`'d inside `releaser/` or from an unrelated directory, we
      default to `SCRIPT_DIR.parent` (the repository containing `releaser/`).
    """
    cwd = Path.cwd().resolve()
    repo_root = SCRIPT_DIR.parent
    if cwd == SCRIPT_DIR or cwd == repo_root:
        return repo_root
    if any(cwd.glob("build.gradle*")) or any(cwd.glob("*/build.gradle*")) or any(cwd.glob("*/*/build.gradle*")):
        return cwd
    return repo_root


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(
        description=(
            "Recursively discover Android application projects in a directory "
            "and populate a human-editable Markdown version table."
        )
    )
    parser.add_argument(
        "--root",
        "-r",
        type=Path,
        default=None,
        help="Top-level directory to scan recursively (default: current directory or repo root).",
    )
    parser.add_argument(
        "--table",
        "-t",
        type=Path,
        default=SCRIPT_DIR / "versions.md",
        help="Path to the Markdown table file to write (default: releaser/versions.md).",
    )
    parser.add_argument(
        "--tag-pattern",
        default="{app}-v{version}",
        help="Default Git tag format string (default: '{app}-v{version}').",
    )
    parser.add_argument(
        "--stdout-only",
        action="store_true",
        help="Print the discovered Markdown table to stdout without writing a file.",
    )
    args = parser.parse_args(argv)

    root_dir = (args.root or resolve_default_root()).resolve()
    releaser = AndroidReleaser(root_dir=root_dir)
    projects = releaser.discover_projects()

    if not projects:
        print(
            f"[ERROR] No Android application projects found under: {root_dir}\n"
            f"  Why this happened:\n"
            f"    `discover_projects()` looks recursively for `build.gradle` or `build.gradle.kts`\n"
            f"    files that apply the `com.android.application` plugin and define `versionName`\n"
            f"    and `versionCode`.\n"
            f"  Suggested fix:\n"
            f"    Pass `--root /path/to/your/android/projects` pointing to the directory that\n"
            f"    contains your Android app module(s).",
            file=sys.stderr,
        )
        return 1

    # Preserve any custom column order if the target Markdown file already exists.
    # Why: If a user customized `versions.md` to put `Project Dir | Version Name | Version Code`
    # first, re-running `scan_versions.py` should refresh the row data without
    # clobbering their preferred column ordering.
    columns = MarkdownVersionTable.DEFAULT_COLUMNS
    table_path = args.table.resolve()
    if table_path.is_file():
        try:
            existing_text = table_path.read_text(encoding="utf-8")
            for line in existing_text.splitlines():
                stripped = line.strip()
                if stripped.startswith("|") and stripped.endswith("|"):
                    raw_headers = [c.strip() for c in stripped.strip("|").split("|")]
                    if all(
                        MarkdownVersionTable._normalize_header(h) is not None
                        for h in raw_headers
                    ) and len(raw_headers) == len(MarkdownVersionTable.COLUMNS):
                        columns = tuple(raw_headers)  # type: ignore[assignment]
                    break
        except OSError:
            pass

    rows = releaser.projects_to_rows(
        projects=projects, default_tag_pattern=args.tag_pattern
    )
    markdown_output = MarkdownVersionTable.dumps(rows, include_header=True, columns=columns)

    print(markdown_output, end="")

    if not args.stdout_only:
        MarkdownVersionTable.write_file(table_path, rows, columns=columns)
        print(
            f"\n[OK] Wrote {len(rows)} Android project(s) from '{root_dir}' to: {table_path}"
        )

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
