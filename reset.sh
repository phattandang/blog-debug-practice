#!/usr/bin/env bash
# Throw away the current drill (and your edits) and go back to the working baseline.
set -euo pipefail
cd "$(dirname "$0")"
git checkout -q -f main
git branch -q -D practice 2>/dev/null || true
rm -f .drill
echo "Back on the working baseline (main)."
