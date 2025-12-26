#!/usr/bin/env sh
set -eu

# 목적: 외부기관 연계가 최소한의 응답을 반환하는지 확인한다.
# 이유: 연계 API 확정 전이라도 스모크 기준을 유지하기 위함이다.
base="http://localhost:18080"
endpoint="/api/external-sync"
max_try=3

health_code="$(curl -s -o /dev/null -w "%{http_code}" "$base/health" || true)"
if [ "$health_code" != "200" ]; then
  printf '%s\n' "[SKIP] external sync"
  exit 2
fi

i=1
while [ "$i" -le "$max_try" ]; do
  code="$(curl -s -o /dev/null -w "%{http_code}" -X POST "$base$endpoint" || true)"
  if [ "$code" = "200" ]; then
    printf '%s\n' "[PASS] external sync"
    exit 0
  fi
  i=$((i + 1))
  sleep 1
done

printf '%s\n' "[FAIL] external sync"
exit 1
