#!/usr/bin/env bash
# ==============================================================================
# submit_to_fdroiddata.sh
# Helper script to copy the generated F-Droid YAML metadata files into a local
# clone of your GitLab fork of https://gitlab.com/fdroid/fdroiddata and create
# one clean feature branch per app as required by F-Droid's Quick Start Guide.
#
# Usage:
#   ./fdroid/submit_to_fdroiddata.sh /path/to/your/fdroiddata [app_id]
# ==============================================================================
set -euo pipefail

if [[ $# -lt 1 ]]; then
    echo "Usage: $0 /path/to/fdroiddata [optional_application_id]"
    echo "Example:"
    echo "  git clone git@gitlab.com:<your-gitlab-username>/fdroiddata.git ~/fdroiddata"
    echo "  $0 ~/fdroiddata"
    exit 1
fi

FDROIDDATA_DIR="$(cd "$1" && pwd)"
FILTER_APP="${2:-}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
METADATA_SRC="$SCRIPT_DIR/metadata"

if [[ ! -d "$FDROIDDATA_DIR/metadata" ]]; then
    echo "Error: '$FDROIDDATA_DIR/metadata' not found. Is '$FDROIDDATA_DIR' a clone of fdroiddata?"
    exit 1
fi

for yml_file in "$METADATA_SRC"/*.yml; do
    app_id="$(basename "$yml_file" .yml)"
    if [[ -n "$FILTER_APP" && "$app_id" != "$FILTER_APP" ]]; then
        continue
    fi
    cp "$yml_file" "$FDROIDDATA_DIR/metadata/$app_id.yml"
    echo "[COPIED] $yml_file -> $FDROIDDATA_DIR/metadata/$app_id.yml"
done

echo ""
echo "Next steps inside $FDROIDDATA_DIR (per F-Droid Quick Start Guide):"
echo "  cd \"$FDROIDDATA_DIR\""
echo "  # Test lint & metadata check using fdroidserver (if installed):"
echo "  fdroid lint com.eggwall.chordwheel"
echo "  fdroid rewritemeta com.eggwall.chordwheel"
echo ""
echo "  # Create a branch per app and push to your GitLab fork to open a Merge Request:"
echo "  git checkout -B add-com.eggwall.chordwheel master"
echo "  git add metadata/com.eggwall.chordwheel.yml"
echo "  git commit -m 'New App: com.eggwall.chordwheel'"
echo "  git push -u origin add-com.eggwall.chordwheel"
