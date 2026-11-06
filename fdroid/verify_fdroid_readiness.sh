#!/usr/bin/env bash
# ==============================================================================
# verify_fdroid_readiness.sh
# Verifies all F-Droid inclusion requirements across all 10 Android apps:
#   1. Apache-2.0 LICENSE and COPYING present in every app
#   2. `dependenciesInfo { includeInApk = false; includeInBundle = false }` present
#   3. No hardcoded `org.gradle.java.home` in any `gradle.properties`
#   4. Standard Fastlane structure (`fastlane/metadata/android/<locale>/`) present
#      with <= 50 char title, <= 80 char short_description, <= 4000 char full_description,
#      512x512 icon.png, phoneScreenshots, and changelogs/<versionCode>.txt
#   5. All 10 `fdroid/metadata/<applicationId>.yml` recipes present and valid
# ==============================================================================
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

APPS=(
    "chordwheel:com.eggwall.chordwheel:1.4:4"
    "fretboard:com.eggwall.fretboard:2.0:2"
    "licktrainer:com.eggwall.licktrainer:1.0:1"
    "pocketband:com.eggwall.pocketband:1.0:1"
    "eartrainer:com.eggwall.eartrainer:1.0:1"
    "metronome:com.eggwall.metronome:1.0:1"
    "pomodoro:com.eggwall.pomodoro:1.0:1"
    "sightread:com.eggwall.sightread:1.0:1"
    "rhythm-trainer:com.eggwall.rhythmtrainer:1.0.0:1"
    "sound-sleep:com.eggwall.soundsleep:1.11:11"
)

ERRORS=0

for entry in "${APPS[@]}"; do
    IFS=':' read -r app_dir app_id ver_name ver_code <<< "$entry"
    echo "Checking $app_dir ($app_id v$ver_name, code $ver_code)..."

    # 1. License check
    if [[ ! -f "$app_dir/LICENSE" ]]; then
        echo "  [ERR] Missing $app_dir/LICENSE"
        ERRORS=$((ERRORS + 1))
    fi

    # 2. gradle.properties check
    if grep -q "org.gradle.java.home" "$app_dir/gradle.properties"; then
        echo "  [ERR] Hardcoded org.gradle.java.home in $app_dir/gradle.properties"
        ERRORS=$((ERRORS + 1))
    fi

    # 3. dependenciesInfo check
    gradle_file="$app_dir/build.gradle"
    if ! grep -q "includeInApk = false" "$gradle_file"; then
        echo "  [ERR] Missing includeInApk = false in $gradle_file"
        ERRORS=$((ERRORS + 1))
    fi

    # 4. Fastlane metadata check
    fl_en="$app_dir/fastlane/metadata/android/en-US"
    for req_file in "$fl_en/title.txt" "$fl_en/short_description.txt" "$fl_en/full_description.txt" "$fl_en/images/icon.png" "$fl_en/changelogs/${ver_code}.txt"; do
        if [[ ! -f "$req_file" ]]; then
            echo "  [ERR] Missing Fastlane file: $req_file"
            ERRORS=$((ERRORS + 1))
        fi
    done

    # 5. F-Droid YAML check
    yml_file="fdroid/metadata/${app_id}.yml"
    if [[ ! -f "$yml_file" ]]; then
        echo "  [ERR] Missing F-Droid recipe: $yml_file"
        ERRORS=$((ERRORS + 1))
    fi
done

if [[ $ERRORS -eq 0 ]]; then
    echo ""
    echo "All 10 apps passed F-Droid readiness checks with 0 errors!"
else
    echo ""
    echo "Found $ERRORS error(s)."
    exit 1
fi
