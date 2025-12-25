#!/usr/bin/env sh
set -eu

# 목적: /health 200 응답 여부만 확인한다.
# 이유: 단순/확실한 상태 확인이 운영 자동화에 유리하다.
code="$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:18080/health" || true)"
if [ "$code" = "200" ]; then
  printf '%s\n' "[PASS] health 200"
  exit 0
fi
printf '%s\n' "[FAIL] health not 200"
exit 1
