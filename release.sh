#!/usr/bin/env bash
# ==============================================================================
# release.sh — Single-Spreadsheet Version Manager, Builder & Tag Generator
#
# To increment the version of an app:
#   1. Edit VERSION_NAME and VERSION_CODE for that app in APP_VERSIONS_TABLE below.
#   2. Run:
#        ./release.sh
#
# What this script does for each updated (or specified) app:
#   1. Updates versionName & versionCode across:
#        - <app>/build.gradle
#        - <app>/AndroidManifest.xml
#        - fdroid/metadata/<app_id>.yml (Builds entry + CurrentVersion/Code)
#        - <app>/fastlane/metadata/android/en-US/changelogs/<versionCode>.txt
#        - fdroid/verify_fdroid_readiness.sh, fdroid/create_fdroid_tags.sh,
#          fdroid/README.md, and BUILD_SYSTEM_REFERENCE.md
#   2. Builds the Play Store Android App Bundle (.aab), Release APK, and Debug APK,
#      and stages version-named GitHub release artifacts in:
#        releases/<app>-v<versionName>/
#   3. Creates/updates the annotated Git tag `<app>-v<versionName>` required by
#      F-Droid (`UpdateCheckMode: Tags ^<app>-v.+$`) and GitHub Releases.
#
# Usage:
#   ./release.sh                  # Auto-detect apps whose version changed in table
#   ./release.sh <app_dir> [...]  # Run full pipeline (sync + build + tag) for app(s)
#   ./release.sh --all            # Run full pipeline for all 10 apps
#   ./release.sh --sync-only      # Only sync version numbers & tags without building
#   ./release.sh --status         # Show version status table across all 10 apps
#   ./release.sh --push           # Push created Git tags to origin
# ==============================================================================
set -euo pipefail

# ==============================================================================
# APP VERSION SPREADSHEET (Single Source of Truth)
# Edit VERSION_NAME and VERSION_CODE below to increment any app's version.
# ==============================================================================
APP_VERSIONS_TABLE="
# APP_DIR        | APP_ID                    | VERSION_NAME | VERSION_CODE | ARTIFACT_PREFIX | DISPLAY_NAME
chordwheel       | com.eggwall.chordwheel    | 1.4          | 4            | ChordWheel      | ChordWheel
eartrainer       | com.eggwall.eartrainer    | 1.0          | 1            | EarTrainer      | Ear Trainer
fretboard        | com.eggwall.fretboard     | 2.0          | 2            | Fretboard       | Fretboard Explorer
licktrainer      | com.eggwall.licktrainer   | 1.0          | 1            | LickTrainer     | Guitar Lick Trainer
metronome        | com.eggwall.metronome     | 1.0          | 1            | Metronome       | Precision Metronome
pocketband       | com.eggwall.pocketband    | 1.0          | 1            | PocketBand      | Band (PocketBand)
pomodoro         | com.eggwall.pomodoro      | 1.0          | 1            | Pomodoro        | Pomodoro Focus
rhythm-trainer   | com.eggwall.rhythmtrainer | 1.0.0        | 1            | RhythmTrainer   | Rhythm Trainer
sightread        | com.eggwall.sightread     | 1.0          | 1            | SightRead       | SightRead
sound-sleep      | com.eggwall.soundsleep    | 1.11         | 11           | SoundSleep      | Sound Sleep
"

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$REPO_ROOT"

MODE="auto"
SYNC_ONLY=false
PUSH_TAGS=false
 declare -a TARGET_APPS=()

for arg in "$@"; do
    case "$arg" in
        --all|-a)
            MODE="all"
            ;;
        --sync-only|-s)
            SYNC_ONLY=true
            ;;
        --status|--list|-l)
            MODE="status"
            ;;
        --push|-p)
            PUSH_TAGS=true
            ;;
        --help|-h)
            sed -n '2,31p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'
            exit 0
            ;;
        -*)
            echo "Unknown option: $arg" >&2
            echo "Run '$0 --help' for usage." >&2
            exit 1
            ;;
        *)
            MODE="explicit"
            TARGET_APPS+=("${arg%/}")
            ;;
    esac
done

trim() {
    local var="$*"
    var="${var#"${var%%[![:space:]]*}"}"
    var="${var%"${var##*[![:space:]]}"}"
    printf '%s' "$var"
}

get_gradle_version_code() {
    local gradle_file="$1/build.gradle"
    sed -nE 's/^[[:space:]]*versionCode[[:space:]]*=[[:space:]]*([0-9]+).*/\1/p' "$gradle_file" | head -n 1
}

get_gradle_version_name() {
    local gradle_file="$1/build.gradle"
    sed -nE "s/^[[:space:]]*versionName[[:space:]]*=[[:space:]]*['\"]([^'\"]+)['\"].*/\1/p" "$gradle_file" | head -n 1
}

get_manifest_version_code() {
    local manifest_file="$1/AndroidManifest.xml"
    sed -nE 's/.*android:versionCode="([0-9]+)".*/\1/p' "$manifest_file" | head -n 1
}

get_manifest_version_name() {
    local manifest_file="$1/AndroidManifest.xml"
    sed -nE 's/.*android:versionName="([^"]+)".*/\1/p' "$manifest_file" | head -n 1
}

get_fdroid_version_code() {
    local yml_file="fdroid/metadata/$1.yml"
    sed -nE 's/^CurrentVersionCode:[[:space:]]*([0-9]+).*/\1/p' "$yml_file" | head -n 1
}

get_fdroid_version_name() {
    local yml_file="fdroid/metadata/$1.yml"
    sed -nE "s/^CurrentVersion:[[:space:]]*['\"]?([^'\"]+)['\"]?.*/\1/p" "$yml_file" | head -n 1
}

is_app_targeted() {
    local candidate="$1"
    local item
    for item in "${TARGET_APPS[@]}"; do
        if [[ "$item" == "$candidate" ]]; then
            return 0
        fi
    done
    return 1
}

update_app_files() {
    local app_dir="$1"
    local app_id="$2"
    local ver_name="$3"
    local ver_code="$4"
    local display_name="$5"
    local tag_name="${app_dir}-v${ver_name}"

    # 1. Update <app>/build.gradle
    local gradle_file="$app_dir/build.gradle"
    sed -i -E "s/^([[:space:]]*versionCode[[:space:]]*=[[:space:]]*)[0-9]+/\1${ver_code}/" "$gradle_file"
    sed -i -E "s/^([[:space:]]*versionName[[:space:]]*=[[:space:]]*)['\"][^'\"]+['\"]/\\1'${ver_name}'/" "$gradle_file"

    # 2. Update <app>/AndroidManifest.xml
    local manifest_file="$app_dir/AndroidManifest.xml"
    sed -i -E "s/(android:versionCode=\")[0-9]+(\")/\1${ver_code}\2/" "$manifest_file"
    sed -i -E "s/(android:versionName=\")[^\"]+(\")/\1${ver_name}\2/" "$manifest_file"

    # 3. Update fdroid/metadata/<app_id>.yml
    local yml_file="fdroid/metadata/${app_id}.yml"
    if [[ -f "$yml_file" ]]; then
        python3 - "$yml_file" "$app_dir" "$ver_name" "$ver_code" "$tag_name" <<'PYEOF'
import re
import sys

yml_path, app_dir, ver_name, ver_code, tag_name = sys.argv[1:]
with open(yml_path, "r", encoding="utf-8") as f:
    content = f.read()

content = re.sub(r"(?m)^CurrentVersion:\s*.*$", f"CurrentVersion: '{ver_name}'", content)
content = re.sub(r"(?m)^CurrentVersionCode:\s*.*$", f"CurrentVersionCode: {ver_code}", content)

build_block = (
    f"  - versionName: '{ver_name}'\n"
    f"    versionCode: {ver_code}\n"
    f"    commit: {tag_name}\n"
    f"    subdir: {app_dir}\n"
    f"    gradle:\n"
    f"      - yes\n"
)

pattern_same_code = re.compile(
    r"  - versionName: '[^']*'\n"
    r"    versionCode: " + re.escape(ver_code) + r"\n"
    r"    commit: [^\n]*\n"
    r"    subdir: [^\n]*\n"
    r"    gradle:\n"
    r"      - yes\n"
)

if pattern_same_code.search(content):
    content = pattern_same_code.sub(build_block, content)
else:
    content = content.replace("\nAutoUpdateMode:", "\n" + build_block + "\nAutoUpdateMode:", 1)

with open(yml_path, "w", encoding="utf-8") as f:
    f.write(content)
PYEOF
    fi

    # 4. Ensure Fastlane changelog <versionCode>.txt exists for Play Store & F-Droid
    local changelog_dir="$app_dir/fastlane/metadata/android/en-US/changelogs"
    mkdir -p "$changelog_dir"
    local changelog_file="$changelog_dir/${ver_code}.txt"
    if [[ ! -f "$changelog_file" ]]; then
        printf "%s v%s (versionCode %s)\n" "$display_name" "$ver_name" "$ver_code" > "$changelog_file"
        echo "  [NEW]  Created Fastlane changelog: $changelog_file"
    fi

    # 5. Sync helper scripts & reference docs (verify_fdroid_readiness.sh, create_fdroid_tags.sh, fdroid/README.md, BUILD_SYSTEM_REFERENCE.md)
    python3 - "$app_dir" "$app_id" "$ver_name" "$ver_code" "$tag_name" <<'PYEOF'
import os
import re
import sys

app_dir, app_id, ver_name, ver_code, tag_name = sys.argv[1:]

def patch_file(path, pattern, replacement):
    if not os.path.exists(path):
        return
    with open(path, "r", encoding="utf-8") as f:
        text = f.read()
    new_text = re.sub(pattern, replacement, text, flags=re.MULTILINE)
    if new_text != text:
        with open(path, "w", encoding="utf-8") as f:
            f.write(new_text)

patch_file(
    "fdroid/verify_fdroid_readiness.sh",
    rf'"{re.escape(app_dir)}:{re.escape(app_id)}:[^"]+"',
    f'"{app_dir}:{app_id}:{ver_name}:{ver_code}"',
)
patch_file(
    "fdroid/create_fdroid_tags.sh",
    rf'"{re.escape(app_dir)}-v[^|]+\|([^"\n]*?)v[^ ]+ \(versionCode [0-9]+\)',
    rf'"{tag_name}|\1v{ver_name} (versionCode {ver_code})',
)
patch_file(
    "fdroid/README.md",
    rf'(\| `{re.escape(app_id)}` \| )`[^`]+` \(`[0-9]+`\)(\s*\| )`{re.escape(app_dir)}-v[^`]+`',
    rf'\g<1>`{ver_name}` (`{ver_code}`)\g<2>`{tag_name}`',
)
patch_file(
    "BUILD_SYSTEM_REFERENCE.md",
    rf'(\| `{re.escape(app_id)}` \| )`[^`]+` \(`[0-9]+`\)',
    rf'\g<1>`{ver_name}` (`{ver_code}`)',
)
PYEOF
}

build_app_artifacts() {
    local app_dir="$1"
    local ver_name="$2"
    local ver_code="$3"
    local prefix="$4"
    local tag_name="${app_dir}-v${ver_name}"
    local out_dir="releases/${tag_name}"

    echo "  [BUILD] Running Gradle bundleRelease, assembleDebug, assembleRelease for ${app_dir}..."
    (
        cd "$app_dir"
        ./gradlew bundleRelease assembleDebug assembleRelease
    )

    local src_aab="${app_dir}/build/outputs/bundle/release/${prefix}-release.aab"
    local src_rel_apk="${app_dir}/build/outputs/apk/release/${prefix}-release.apk"
    local src_dbg_apk="${app_dir}/build/outputs/apk/debug/${prefix}-debug.apk"

    for f in "$src_aab" "$src_rel_apk" "$src_dbg_apk"; do
        if [[ ! -f "$f" ]]; then
            echo "  [ERR] Expected build artifact not found: $f" >&2
            exit 1
        fi
    done

    mkdir -p "$out_dir"
    local dst_aab="${out_dir}/${app_dir}-v${ver_name}-playstore-release.aab"
    local dst_rel_apk="${out_dir}/${app_dir}-v${ver_name}-release.apk"
    local dst_dbg_apk="${out_dir}/${app_dir}-v${ver_name}-debug.apk"

    cp "$src_aab" "$dst_aab"
    cp "$src_rel_apk" "$dst_rel_apk"
    cp "$src_dbg_apk" "$dst_dbg_apk"

    (
        cd "$out_dir"
        sha256sum "$(basename "$dst_aab")" "$(basename "$dst_rel_apk")" "$(basename "$dst_dbg_apk")" > SHA256SUMS.txt
    )

    echo "  [OK]    Play Store Bundle : $dst_aab"
    echo "  [OK]    GitHub Release APK: $dst_rel_apk"
    echo "  [OK]    GitHub Debug APK  : $dst_dbg_apk"
    echo "  [OK]    Checksums         : ${out_dir}/SHA256SUMS.txt"
}

create_app_tag() {
    local app_dir="$1"
    local ver_name="$2"
    local ver_code="$3"
    local display_name="$4"
    local tag_name="${app_dir}-v${ver_name}"
    local tag_msg="${display_name} v${ver_name} (versionCode ${ver_code}) — F-Droid & GitHub Release"

    git tag -a -f "$tag_name" -m "$tag_msg"
    echo "  [TAG]   ${tag_name} -> $(git rev-parse --short HEAD) (\"${tag_msg}\")"
}

print_status_table() {
    printf "%-16s | %-25s | %-10s | %-6s | %-10s | %-22s | %-10s\n" \
        "APP_DIR" "APP_ID" "TABLE_VER" "CODE" "GRADLE_VER" "GIT_TAG" "ARTIFACTS"
    printf "%s\n" "-----------------+---------------------------+------------+--------+------------+------------------------+-----------"

    while IFS='|' read -r raw_dir raw_id raw_vname raw_vcode raw_prefix raw_dname; do
        local line_Check
        line_Check="$(trim "${raw_dir:-}")"
        [[ -z "$line_Check" || "$line_Check" == \#* ]] && continue

        local app_dir app_id ver_name ver_code
        app_dir="$(trim "$raw_dir")"
        app_id="$(trim "$raw_id")"
        ver_name="$(trim "$raw_vname")"
        ver_code="$(trim "$raw_vcode")"

        local g_name g_code tag_name tag_state art_state
        g_name="$(get_gradle_version_name "$app_dir")"
        g_code="$(get_gradle_version_code "$app_dir")"
        tag_name="${app_dir}-v${ver_name}"

        if git rev-parse -q --verify "refs/tags/${tag_name}" >/dev/null 2>&1; then
            tag_state="$tag_name (ok)"
        else
            tag_state="$tag_name (MISSING)"
        fi

        if [[ -f "releases/${tag_name}/${app_dir}-v${ver_name}-release.apk" && \
              -f "releases/${tag_name}/${app_dir}-v${ver_name}-playstore-release.aab" ]]; then
            art_state="ready"
        else
            art_state="not built"
        fi

        printf "%-16s | %-25s | %-10s | %-6s | %-10s | %-22s | %-10s\n" \
            "$app_dir" "$app_id" "$ver_name" "$ver_code" "${g_name} (${g_code})" "$tag_state" "$art_state"
    done <<< "$APP_VERSIONS_TABLE"
}

if [[ "$MODE" == "status" ]]; then
    print_status_table
    exit 0
fi

declare -a PROCESSED_TAGS=()
PROCESSED_COUNT=0

while IFS='|' read -r raw_dir raw_id raw_vname raw_vcode raw_prefix raw_dname; do
    line_check="$(trim "${raw_dir:-}")"
    [[ -z "$line_check" || "$line_check" == \#* ]] && continue

    app_dir="$(trim "$raw_dir")"
    app_id="$(trim "$raw_id")"
    ver_name="$(trim "$raw_vname")"
    ver_code="$(trim "$raw_vcode")"
    prefix="$(trim "$raw_prefix")"
    display_name="$(trim "$raw_dname")"
    tag_name="${app_dir}-v${ver_name}"

    g_name="$(get_gradle_version_name "$app_dir")"
    g_code="$(get_gradle_version_code "$app_dir")"
    m_name="$(get_manifest_version_name "$app_dir")"
    m_code="$(get_manifest_version_code "$app_dir")"
    f_name="$(get_fdroid_version_name "$app_id")"
    f_code="$(get_fdroid_version_code "$app_id")"

    version_changed=false
    if [[ "$ver_name" != "$g_name" || "$ver_code" != "$g_code" || \
          "$ver_name" != "$m_name" || "$ver_code" != "$m_code" || \
          "$ver_name" != "$f_name" || "$ver_code" != "$f_code" ]]; then
        version_changed=true
    fi

    should_process=false
    if [[ "$MODE" == "all" ]]; then
        should_process=true
    elif [[ "$MODE" == "explicit" ]]; then
        if is_app_targeted "$app_dir"; then
            should_process=true
        fi
    elif [[ "$MODE" == "auto" ]]; then
        if [[ "$version_changed" == "true" ]]; then
            should_process=true
        fi
    fi

    if [[ "$should_process" == "true" ]]; then
        echo "=============================================================================="
        echo "Processing ${display_name} (${app_dir}) -> versionName=${ver_name}, versionCode=${ver_code}"
        echo "=============================================================================="
        update_app_files "$app_dir" "$app_id" "$ver_name" "$ver_code" "$display_name"
        echo "  [SYNC]  Updated build.gradle, AndroidManifest.xml, F-Droid YAML & docs."

        if [[ "$SYNC_ONLY" == "false" ]]; then
            build_app_artifacts "$app_dir" "$ver_name" "$ver_code" "$prefix"
        fi

        create_app_tag "$app_dir" "$ver_name" "$ver_code" "$display_name"
        PROCESSED_TAGS+=("$tag_name")
        PROCESSED_COUNT=$((PROCESSED_COUNT + 1))
        echo ""
    elif [[ "$MODE" == "auto" ]]; then
        # Even if version did not change, ensure the F-Droid/GitHub release tag exists locally
        if ! git rev-parse -q --verify "refs/tags/${tag_name}" >/dev/null 2>&1; then
            echo "[INIT-TAG] Creating initial release tag for ${app_dir} (${tag_name})..."
            create_app_tag "$app_dir" "$ver_name" "$ver_code" "$display_name"
            PROCESSED_TAGS+=("$tag_name")
        fi
    fi
done <<< "$APP_VERSIONS_TABLE"

if [[ $PROCESSED_COUNT -eq 0 && "$MODE" == "auto" ]]; then
    echo "All 10 apps in build.gradle, AndroidManifest.xml, and F-Droid metadata already match APP_VERSIONS_TABLE."
    echo ""
    print_status_table
    echo ""
    echo "Tip: Edit VERSION_NAME & VERSION_CODE in APP_VERSIONS_TABLE at the top of ./release.sh"
    echo "     and re-run ./release.sh, or run './release.sh <app_dir>' / './release.sh --all' to force a build."
fi

if [[ "$PUSH_TAGS" == "true" && ${#PROCESSED_TAGS[@]} -gt 0 ]]; then
    echo "Pushing tags to origin: ${PROCESSED_TAGS[*]}"
    git push origin --force "${PROCESSED_TAGS[@]}"
elif [[ ${#PROCESSED_TAGS[@]} -gt 0 ]]; then
    echo "------------------------------------------------------------------------------"
    echo "Created/updated tags: ${PROCESSED_TAGS[*]}"
    echo "To push tags to GitHub (for F-Droid & GitHub Releases):"
    echo "  git push origin ${PROCESSED_TAGS[*]}"
    echo "To upload artifacts to a GitHub Release (using GitHub CLI 'gh'):"
    for t in "${PROCESSED_TAGS[@]}"; do
        if [[ -d "releases/${t}" ]]; then
            echo "  gh release create ${t} releases/${t}/* --title \"${t}\" --generate-notes"
        fi
    done
    echo "------------------------------------------------------------------------------"
fi
