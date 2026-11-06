#!/usr/bin/env bash
# ==============================================================================
# create_fdroid_tags.sh
# Creates (and optionally pushes) annotated Git release tags for all 10 Android
# apps in the monorepo so F-Droid's `fdroidserver` (`UpdateCheckMode: Tags`) can
# build each app from its exact release tag.
#
# Usage:
#   ./fdroid/create_fdroid_tags.sh           # Create annotated tags locally
#   ./fdroid/create_fdroid_tags.sh --push    # Create tags locally and push to origin
#   ./fdroid/create_fdroid_tags.sh --force   # Overwrite existing tags locally
# ==============================================================================
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

PUSH_TAGS=false
FORCE_FLAG=()

for arg in "$@"; do
    case "$arg" in
        --push)
            PUSH_TAGS=true
            ;;
        --force|-f)
            FORCE_FLAG=(-f)
            ;;
        *)
            echo "Unknown option: $arg"
            echo "Usage: $0 [--push] [--force]"
            exit 1
            ;;
    esac
done

# Warn if working tree has uncommitted changes
if ! git diff --quiet || ! git diff --cached --quiet; then
    echo "WARNING: You have uncommitted or staged changes in $REPO_ROOT."
    echo "F-Droid builds from the tagged commit, so commit your changes first before tagging for production!"
    echo ""
fi

declare -a TAG_SPECS=(
    "chordwheel-v1.4|ChordWheel v1.4 (versionCode 4) — Circle of Fifths & Guitar Chord Looper"
    "fretboard-v2.0|Fretboard Explorer v2.0 (versionCode 2) — Guitar Scales, CAGED Boxes & Chords"
    "licktrainer-v1.0|Guitar Lick Trainer v1.0 (versionCode 1) — 33 Guitar Licks & Speed Ladder"
    "pocketband-v1.0|Band (PocketBand) v1.0 (versionCode 1) — 4-Track Step Sequencer & Backing Studio"
    "eartrainer-v1.0|Ear Trainer v1.0 (versionCode 1) — Interval, Chord, Solfege & Melodic Ear Training"
    "metronome-v1.0|Precision Metronome v1.0 (versionCode 1) — Pendulum, Odd Meters & Polyrhythms"
    "pomodoro-v1.0|Pomodoro Focus v1.0 (versionCode 1) — Focus Timer, Ambient Soundscapes & Telemetry"
    "sightread-v1.0|SightRead v1.0 (versionCode 1) — Treble Clef Guitar Sight-Reading Trainer"
    "rhythm-trainer-v1.0.0|Rhythm Trainer v1.0.0 (versionCode 1) — 440 Hz Rhythm Notation Editor & Player"
    "sound-sleep-v1.11|Sound Sleep v1.11 (versionCode 11) — Bedside Night Clock, White Noise & Sleep Music"
)

CREATED_TAGS=()

for spec in "${TAG_SPECS[@]}"; do
    IFS='|' read -r tag_name tag_msg <<< "$spec"
    if git rev-parse "$tag_name" >/dev/null 2>&1 && [[ ${#FORCE_FLAG[@]} -eq 0 ]]; then
        echo "[SKIP] Tag '$tag_name' already exists (pass --force to recreate)."
    else
        git tag -a "${FORCE_FLAG[@]}" "$tag_name" -m "$tag_msg"
        echo "[OK]   Created annotated tag: $tag_name  ->  $(git rev-parse --short HEAD)"
    fi
    CREATED_TAGS+=("$tag_name")
done

echo ""
echo "Current F-Droid release tags:"
git tag -l "*-v*"

if [[ "$PUSH_TAGS" == "true" ]]; then
    echo ""
    echo "Pushing tags to origin..."
    git push origin "${FORCE_FLAG[@]}" "${CREATED_TAGS[@]}"
    echo "All F-Droid tags pushed to origin."
else
    echo ""
    echo "To push these tags to GitHub (origin), run:"
    echo "  ./fdroid/create_fdroid_tags.sh --push"
    echo "or manually:"
    echo "  git push origin ${CREATED_TAGS[*]}"
fi
