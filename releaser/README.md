# `releaser` — Android Version Table, Environment Auditor, F-Droid Verifier & Release Runner

`releaser` is a zero-dependency Python 3 package and CLI toolkit for managing versions, auditing the Android build toolchain (Java/JDK, Gradle, Android Gradle Plugin, Android SDK), verifying F-Droid readiness, building Google Play Store bundles (`.aab`) + GitHub/F-Droid APKs (`.apk`), and creating annotated release Git tags across one or many Android projects.

---

## Table of Contents
1. [How to Use `releaser` (CLI Workflow)](#1-how-to-use-releaser-cli-workflow)
2. [Auditing the Build Environment & F-Droid Readiness (`--check`)](#2-auditing-the-build-environment--f-droid-readiness---check)
3. [Using `AndroidReleaser` as a Python Library or Binary](#3-using-androidreleaser-as-a-python-library-or-binary)
4. [Assumptions the Code Makes](#4-assumptions-the-code-makes)
5. [Where This Code Can Be Used](#5-where-this-code-can-be-used)
6. [How to Modify and Extend the Code](#6-how-to-modify-and-extend-the-code)
7. [Running Unit Tests](#7-running-unit-tests)

---

## 1. How to Use `releaser` (CLI Workflow)

### Step 1: Populate or Refresh the Markdown Version Table
Run [`scan_versions.py`](scan_versions.py) to recursively inspect the repository (or any `--root` directory) and generate [`versions.md`](versions.md):

```bash
./releaser/scan_versions.py
```

This writes a clean, human-editable Markdown table to [`releaser/versions.md`](versions.md). If `versions.md` already exists with a custom column order, `scan_versions.py` preserves your column order when refreshing rows:

```markdown
| Project Dir    | Version Name | Version Code | App ID                    | Display Name        | Tag Pattern      |
| -------------- | ------------ | ------------ | ------------------------- | ------------------- | ---------------- |
| chordwheel     |          1.4 |            4 | com.eggwall.chordwheel    | ChordWheel          | {app}-v{version} |
| eartrainer     |          1.0 |            1 | com.eggwall.eartrainer    | Ear Trainer         | {app}-v{version} |
| fretboard      |          2.0 |            2 | com.eggwall.fretboard     | Fretboard Explorer  | {app}-v{version} |
...
```

### Step 2: Edit `versions.md`
Open [`releaser/versions.md`](versions.md) in any text editor:
- Increment **Version Name** (user-visible string, e.g., `1.4` -> `1.5`) and **Version Code** (monotonically increasing integer required by Google Play and F-Droid, e.g., `4` -> `5`) for the app(s) you want to release.
- You may reorder the columns in `versions.md` in any way you like; [`MarkdownVersionTable.loads`](core.py) decodes each column dynamically from the first header row.

### Step 3: Run `release.py`
```bash
./releaser/release.py
```

For every project whose row in `versions.md` differs from disk (or any project explicitly named on the command line), [`release.py`](release.py) performs:

1. **Pre-Flight Build Environment Check**:
   - Inspects your active JDK (`JAVA_HOME` / `java -version`), Android SDK (`ANDROID_HOME`), Gradle Wrapper version, Android Gradle Plugin (AGP) version vs. `compileSdk`, `targetSdk`, and Java `sourceCompatibility` / `targetCompatibility`.
2. **Version Synchronization**:
   - Updates `versionName` and `versionCode` in `build.gradle` or `build.gradle.kts`.
   - Updates `android:versionName` and `android:versionCode` in `AndroidManifest.xml` (if present).
   - Appends a new build entry (and updates `CurrentVersion` / `CurrentVersionCode`) in F-Droid metadata (`fdroid/metadata/<app_id>.yml`) if present, while preserving historical `Builds:` entries.
   - Creates `<fastlane>/en-US/changelogs/<versionCode>.txt` if Fastlane metadata is present.
3. **Automatic F-Droid Readiness Verification** (whenever any F-Droid `.yml` files exist):
   - Verifies open-source `LICENSE`, confirms `gradle.properties` has no hardcoded `org.gradle.java.home`, verifies `dependenciesInfo { includeInApk = false; includeInBundle = false }` in `build.gradle`, checks that no prebuilt `.jar`/`.aar`/`.so` binaries are checked into the source tree, validates Fastlane metadata character limits (`title.txt` <= 50 chars, `short_description.txt` <= 80 chars, `full_description.txt` <= 4000 chars), screenshots, icon, changelog `<versionCode>.txt`, and verifies the F-Droid `.yml` recipe.
4. **Play Store & GitHub Artifact Build**:
   - Runs `./gradlew bundleRelease assembleDebug assembleRelease` inside the project.
   - Stages artifacts in `releases/<tag>/`:
     - `<app>-v<version>-playstore-release.aab` (Google Play Store Android App Bundle)
     - `<app>-v<version>-release.apk` (Release APK for GitHub Releases / F-Droid)
     - `<app>-v<version>-debug.apk` (Debug APK for testing)
     - `SHA256SUMS.txt` (SHA-256 checksums for release verification)
5. **Annotated Git Tagging**:
   - Creates/updates an annotated Git tag formatted using the row's `Tag Pattern` (e.g., `chordwheel-v1.5`).

### Common CLI Flags

```bash
# Audit Java/JDK, Gradle, AGP, Android SDK, and F-Droid readiness across all apps:
./releaser/release.py --check

# Compare versions.md against on-disk files, F-Droid status, Git tags, and built artifacts:
./releaser/release.py --status

# Preview what would be updated, built, and tagged without touching disk or running Gradle:
./releaser/release.py --dry-run

# Force a release build + tag for specific app(s) even if versions.md is already in sync:
./releaser/release.py chordwheel fretboard

# Update version files and Git tags only (skip running Gradle builds):
./releaser/release.py --sync-only

# Create tags and immediately push them to 'origin':
./releaser/release.py --push

# Run against a different directory tree and custom Markdown table location:
./releaser/scan_versions.py --root /path/to/other/repo --table /path/to/versions.md
./releaser/release.py --root /path/to/other/repo --table /path/to/versions.md
```

---

## 2. Auditing the Build Environment & F-Droid Readiness (`--check`)

Running `./releaser/release.py --check` (or `--doctor`) executes two comprehensive diagnostic suites and prints human-readable explanations (`Why this matters:` and `Suggested fix:`) for every finding:

### What `check_build_environment()` Checks:
1. **Java / JDK Compatibility**:
   - Detects the active `JAVA_HOME` / `java` binary on `PATH` and parses its major version.
   - **Why**: Gradle embeds the ASM bytecode library, which rejects class files from JDKs newer than that Gradle release supports (e.g., JDK 25 produces class file major version 69, crashing Gradle < 8.14). Additionally, AGP's `JdkImageTransform` runs `jlink` on the active JDK when compiling Java 9+ language levels and fails on early-access/bleeding-edge JDKs (25/26).
   - Also inspects each project's `gradlew` script to see if it has a defensive fallback to a local JDK 21 LTS installation when the system default is JDK 25+.
2. **Android SDK (`ANDROID_HOME` / `ANDROID_SDK_ROOT`)**:
   - Checks that the Android SDK directory exists, that every `platforms/android-<compileSdk>` directory required by your apps is installed, and that `build-tools/` is present.
3. **Gradle Wrapper (`gradle-wrapper.properties`)**:
   - Reads the Gradle version configured in each project's `gradle/wrapper/gradle-wrapper.properties`, checks that it is high enough for the project's AGP version (`>= 8.11.1` for AGP `8.13.x`), and suggests upgrading if an older wrapper is in use.
4. **Android Gradle Plugin (AGP) vs. `compileSdk`**:
   - Verifies that each app's `com.android.application` plugin version has been officially tested against its `compileSdk` (`compileSdk 36` requires AGP `>= 8.11.0`, recommended `8.13.2`; `compileSdk 35` requires AGP `>= 8.6.0`).
   - Checks whether all apps in a multi-app repository use the same AGP version so the Gradle daemon can reuse a single cached classpath across builds.
5. **Google Play `targetSdk` Policy Floor**:
   - Warns or suggests an upgrade if any app's `targetSdk` falls below Google Play Console's API level floor for new app updates (`API 35+`).
6. **Java `sourceCompatibility` / `targetCompatibility`**:
   - Suggests migrating from legacy `VERSION_1_8` to `VERSION_17` or `VERSION_21` when building with modern AGP 8.x.

### What `check_fdroid_readiness()` Checks (Automatic When `.yml` Files Exist):
Whenever any F-Droid `.yml` file is detected in `fdroid/metadata/<app_id>.yml` or `<project>/.fdroid.yml`, `releaser` assumes the repository targets F-Droid and automatically runs all checks ported from `fdroid/verify_fdroid_readiness.sh`:
1. **Root `LICENSE` File**: Required by F-Droid's open-source inclusion policy.
2. **No Hardcoded `org.gradle.java.home` in `gradle.properties`**: A hardcoded workstation path causes immediate build failure inside F-Droid's Debian build server container.
3. **`dependenciesInfo` Disabled in `build.gradle`**: Verifies `includeInApk = false` and `includeInBundle = false` so AGP does not inject Google's proprietary `DEPENDENCY_INFO_BLOCK` into the APK signing block.
4. **Zero Prebuilt Binary Blobs**: Scans each project (excluding `build/` and `.gradle/`) for checked-in `.jar`, `.aar`, or `.so` files (allowing only `gradle/wrapper/gradle-wrapper.jar`).
5. **Fastlane Metadata & Character Limits**: Verifies `title.txt` (<= 50 chars), `short_description.txt` (<= 80 chars), `full_description.txt` (<= 4000 chars), `images/icon.png`, `images/phoneScreenshots/*.png`, and `changelogs/<versionCode>.txt`.
6. **F-Droid YAML Recipe Sync**: Confirms `CurrentVersion` and `CurrentVersionCode` in `fdroid/metadata/<app_id>.yml` match `build.gradle` and that a `Builds:` block exists for the current `versionCode`.

---

## 3. Using `AndroidReleaser` as a Python Library or Binary

All discovery, parsing, environment auditing, F-Droid checking, version syncing, building, and tagging logic is encapsulated in [`releaser/core.py`](core.py) and exported from [`releaser/__init__.py`](__init__.py). It uses **only the Python 3 standard library**, making it trivial to bundle into a standalone executable (`pex`, `shiv`, `pyinstaller`, `zipapp`, or Bazel `py_binary`).

```python
from releaser import AndroidReleaser, MarkdownVersionTable

releaser = AndroidReleaser(root_dir="/path/to/android/repo")
projects = releaser.discover_projects()

# 1. Run build environment & F-Droid readiness checks programmatically
for finding in releaser.check_build_environment(projects):
    if finding.severity != "OK":
        print(finding.format_verbose())

if releaser.has_any_fdroid_metadata(projects):
    for finding in releaser.check_all_fdroid_readiness(projects):
        if finding.severity != "OK":
            print(finding.format_verbose())

# 2. Update version, build Play Store .aab + APKs, and tag a project
proj = projects[0]
releaser.update_project_version(proj, new_version_name="1.5", new_version_code=5)
artifacts = releaser.build_project_artifacts(proj)
releaser.create_git_tag(proj, push=False)
```

---

## 4. Assumptions the Code Makes

Understanding the assumptions built into `releaser` helps you know what project conventions it expects and when you might want to customize it:

1. **Android Application Marker**:
   - A directory is recognized as an Android application module if its `build.gradle` or `build.gradle.kts` contains the string `com.android.application` and declares integer `versionCode` and string `versionName` literals (using either `=` assignment or Groovy space-separated syntax).
   - Projects that compute `versionCode` dynamically via external functions or external `.properties` files will not match the static regex unless `VERSION_CODE_GET_RE` / `VERSION_NAME_GET_RE` in [`core.py`](core.py) are adjusted.
2. **Project Layouts Supported**:
   - **Flat single-module layout**: `<project>/build.gradle[.kts]` with `./gradlew` in `<project>/` or a parent directory.
   - **Standard Android Studio multi-module layout**: `<project>/app/build.gradle[.kts]` with `./gradlew` in `<project>/`. When `build.gradle[.kts]` is inside a directory named `app/`, `releaser` automatically treats the parent directory as the project root (`project_dir`).
3. **Gradle Wrapper (`gradlew`)**:
   - Building artifacts assumes an executable `gradlew` script exists either in the project directory or in an ancestor directory up to `root_dir`, and that standard Android tasks `bundleRelease`, `assembleDebug`, and `assembleRelease` exist (i.e. no required product flavor dimension like `assembleFreeRelease` unless customized).
4. **F-Droid Metadata Locations**:
   - F-Droid YAML files are looked up at `<root>/fdroid/metadata/<app_id>.yml`, `<root>/metadata/<app_id>.yml`, or `<project>/.fdroid.yml`.
   - If *any* project in the repository has an F-Droid `.yml` file, `release.py` assumes F-Droid readiness checks should be run automatically.
5. **Fastlane Metadata Locations**:
   - Fastlane English (`en-US`) metadata is looked up at `<project>/fastlane/metadata/android/en-US` (or `<project>/app/src/main/play/listings/en-US`).
6. **Git Repository Structure**:
   - Git tag operations run `git` inside `project.project_dir`, which works seamlessly whether all apps share a single monorepo `.git` at `root_dir` or each app directory is its own independent Git repository / submodule.

---

## 5. Where This Code Can Be Used

- **Multi-App Android Monorepos** (like this repository with 10 independent apps): Manage all app versions from a single `versions.md` table, verify toolchain consistency across all apps, and release only the apps whose rows you edited.
- **Single-App Android Repositories**: Point `--root` at a standard single-app repo (with either `app/build.gradle.kts` or root `build.gradle`). Use `Tag Pattern: v{version}` in `versions.md` for standard single-repo tags (`v1.2.0`).
- **Multi-Repo Workspace Directories**: Point `--root` at a parent workspace folder containing several separate cloned Git repositories; because `create_git_tag` executes `git` inside each `project.project_dir`, each sub-repository receives its own Git tag.
- **CI / Pre-Release Verification Pipelines**: Run `./releaser/release.py --check` in GitHub Actions or GitLab CI on every pull request to catch AGP/SDK mismatches, accidental `org.gradle.java.home` commits, missing Fastlane changelogs, or missing `dependenciesInfo` blocks before merging.

---

## 6. How to Modify and Extend the Code

The codebase is intentionally split into three small files so modifications stay localized:
- [`releaser/core.py`](core.py): Data models (`VersionRow`, `AndroidProject`, `ReleaseArtifacts`, `CheckFinding`), `MarkdownVersionTable` parser/formatter, and `AndroidReleaser` business logic.
- [`releaser/release.py`](release.py): Main CLI runner (`--check`, `--status`, `--scan`, `--dry-run`, release loop).
- [`releaser/scan_versions.py`](scan_versions.py): Standalone CLI for scanning directories and generating `versions.md`.

### Common Modifications

#### A. Reordering or Renaming Columns in `versions.md`
- **Reordering**: Simply reorder the columns directly in [`versions.md`](versions.md)! Both `release.py` and `scan_versions.py` inspect the first row of the Markdown table to determine which column is which.
- **Adding a Header Alias**: If you want to rename a column header (e.g., `"Package Name"` as an alias for `"App ID"`), add the normalized lowercase key to `MarkdownVersionTable.HEADER_ALIASES` in [`releaser/core.py`](core.py).
- **Adding a Brand-New Column**:
  1. Add the field to the `VersionRow` dataclass in [`releaser/core.py`](core.py).
  2. Add its canonical key to `MarkdownVersionTable.DEFAULT_COLUMNS`, `COLUMN_TITLES`, and `HEADER_ALIASES`.
  3. Include it in `_row_cell_map()` and `loads()` in `MarkdownVersionTable`.

#### B. Updating SDK, AGP, Gradle, or JDK Thresholds as New Android Versions Ship
At the top of `AndroidReleaser` in [`releaser/core.py`](core.py), update the class constants:
- `MIN_AGP_FOR_COMPILE_SDK`: Maps `compileSdk` API levels (e.g., `36`, `37`) to `(min_agp_version, recommended_agp_version)`.
- `RECOMMENDED_GRADLE_VERSION` / `MIN_GRADLE_FOR_AGP_8_13`: Controls Gradle Wrapper recommendations.
- `RECOMMENDED_JDK_MAJOR` (`21`) and `MAX_TESTED_JDK_MAJOR` (`24`): Controls JDK compatibility warnings.
- `PLAY_STORE_MIN_TARGET_SDK` (`35`): Controls the Google Play `targetSdk` warning floor.

#### C. Adding a New Environment or F-Droid Check
In [`releaser/core.py`](core.py), append a `CheckFinding` object inside `check_build_environment()` or `check_fdroid_readiness()`:
```python
findings.append(
    CheckFinding(
        severity="WARNING",          # "ERROR", "WARNING", "SUGGESTION", or "OK"
        scope=proj.rel_project_dir,  # "environment" or project path
        category="Signing Config",
        summary="Release signing keystore is not configured.",
        explanation="Google Play requires release bundles (.aab) to be signed with your upload key.",
        remediation="Configure `signingConfigs.release` in build.gradle.",
    )
)
```

#### D. Customizing Gradle Tasks (e.g., Product Flavors)
If your project uses product flavors (such as `fdroid` and `play` flavors), pass a custom `gradle_tasks` sequence to `releaser.build_project_artifacts(proj, gradle_tasks=("bundlePlayRelease", "assembleFdroidRelease", "assembleFdroidDebug"))` or update the default tuple in `AndroidReleaser.build_project_artifacts()` in [`releaser/core.py`](core.py).

---

## 7. Running Unit Tests

Run the test suite from the repository root:

```bash
python3 -m unittest discover -s releaser/tests -v
```
