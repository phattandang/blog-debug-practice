#!/usr/bin/env bash
# Stop the timer, run the test suites, and log the result.
set -uo pipefail
cd "$(dirname "$0")"
[ -f .drill ] || { echo "No drill in progress. Start one with ./break.sh" >&2; exit 1; }
source .drill
elapsed=$(( $(date +%s) - start ))
mins=$(( elapsed / 60 )); secs=$(( elapsed % 60 ))

if ./check.sh; then
  result=pass
  echo
  echo "All tests pass. Time: ${mins}m ${secs}s"
else
  result=fail
  echo
  echo "Tests still failing after ${mins}m ${secs}s. Keep going (timer is still running),"
  echo "or ./reset.sh to give up and look at drills/SOLUTIONS.md."
  exit 1
fi

echo "Note: some bugs are invisible to the tests (browser-only)."
echo "Make sure you also confirmed the fix in the browser."
read -r -p "Confirmed in the browser? [y/N] " ok
[ "${ok:-n}" = "y" ] || { echo "Go check, then run ./done.sh again."; exit 1; }

[ -f drills/log.csv ] || echo "date,drills,seconds,result" > drills/log.csv
echo "$(date +%F_%H:%M),${drills// /+},$elapsed,$result" >> drills/log.csv
echo "Logged to drills/log.csv. The bug was drill(s): $drills  (see drills/SOLUTIONS.md)"
echo "Your fix is on branch 'practice'. Compare it to the original with:  git diff main"
echo "Run ./reset.sh before the next drill."
