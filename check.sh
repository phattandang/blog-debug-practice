#!/usr/bin/env bash
# Run all backend and frontend tests. Exit code 0 means everything passed.
cd "$(dirname "$0")"
status=0

echo "==> Backend tests (mvn test)"
if (cd backend && ./mvnw -q test) > .backend-test.log 2>&1; then
  echo "   passed"
else
  status=1
  echo "   FAILED. Full log: .backend-test.log"
  echo "   Tip: read the FIRST failure, not the last line:"
  echo "        grep -n -m5 -E '<<< FAIL|Caused by|Expected|expected' .backend-test.log"
fi

echo "==> Frontend tests (vitest)"
if (cd frontend && npx vitest run) > .frontend-test.log 2>&1; then
  echo "   passed"
else
  status=1
  echo "   FAILED. Full log: .frontend-test.log"
fi

exit $status
