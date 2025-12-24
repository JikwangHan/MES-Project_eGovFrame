#!/usr/bin/env sh
set -eu
code="$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:18080/health" || true)"
if [ "$code" = "200" ]; then
  printf '%s\n' "[PASS] health 200"
  exit 0
fi
printf '%s\n' "[FAIL] health not 200"
exit 1
