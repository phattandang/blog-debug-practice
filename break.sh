#!/usr/bin/env bash
# Plant a bug (or two) on a throwaway "practice" branch.
#   ./break.sh            random drill
#   ./break.sh 7          specific drill
#   ./break.sh hard       two random drills at once
set -euo pipefail
cd "$(dirname "$0")"

if [ -n "$(git status --porcelain)" ]; then
  echo "Working tree has uncommitted changes. Run ./reset.sh first (or commit/stash your edits)." >&2
  exit 1
fi
if [ "$(git rev-parse --abbrev-ref HEAD)" != "main" ]; then
  echo "A drill is already in progress (branch: $(git rev-parse --abbrev-ref HEAD)). Run ./done.sh or ./reset.sh first." >&2
  exit 1
fi

all=()
for f in drills/patches/*.patch; do all+=("$(basename "$f" .patch)"); done

pick_random() { printf '%s\n' "${all[@]}" | awk 'BEGIN{srand()} {a[NR]=$0} END{print a[int(rand()*NR)+1]}'; }

mode="${1:-random}"
picked=()
case "$mode" in
  random) picked=("$(pick_random)") ;;
  hard)
    first="$(pick_random)"; picked=("$first")
    while [ "${#picked[@]}" -lt 2 ]; do
      second="$(pick_random)"; [ "$second" != "$first" ] && picked+=("$second")
    done ;;
  *) n="$(printf '%02d' "$((10#$mode))")"
     [ -f "drills/patches/$n.patch" ] || { echo "No drill $n" >&2; exit 1; }
     picked=("$n") ;;
esac

git checkout -q -B practice main
for id in "${picked[@]}"; do
  if ! git apply "drills/patches/$id.patch" 2>/dev/null; then
    # Two drills touched the same lines; fall back to just the first one.
    echo "(drills overlap, using one bug only)"
    git checkout -q -- .
    picked=("${picked[0]}")
    git apply "drills/patches/${picked[0]}.patch"
    break
  fi
done
git add -A
git -c user.name="A Teammate" -c user.email="teammate@example.com" commit -q -m "Small fixes and cleanup"

{
  echo "drills=${picked[*]}"
  echo "start=$(date +%s)"
} > .drill

echo
echo "================ BUG TICKET ================"
for id in "${picked[@]}"; do
  grep "^$id|" drills/tickets.txt | cut -d'|' -f2- | fold -s -w 70 | sed 's/^/  /'
  echo
done
echo "============================================"
echo "Timer started. Restart the backend and frontend if they are running,"
echo "then reproduce, diagnose, fix. Run ./done.sh when you think it's fixed."
