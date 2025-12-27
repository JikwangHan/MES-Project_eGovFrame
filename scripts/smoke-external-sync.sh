#!/usr/bin/env sh
set -eu

# 목적: 외부기관 연계가 최소한의 응답을 반환하는지 확인한다.
# 이유: 연계 API 확정 전이라도 스모크 기준을 유지하기 위함이다.
base="${MES_WEB_BASE_URL:-http://localhost:18082}"
endpoint="/api/external-sync"
max_try=3
from_date="$(date -u -d "-7 day" +%Y-%m-%d 2>/dev/null || date -u -v-7d +%Y-%m-%d)"
to_date="$(date -u +%Y-%m-%d)"
payload="{\"from\":\"${from_date}\",\"to\":\"${to_date}\"}"

health_code="$(curl -s -o /dev/null -w "%{http_code}" "$base/health" || true)"
if [ "$health_code" != "200" ]; then
  printf '%s\n' "[SKIP] external sync"
  exit 2
fi

i=1
while [ "$i" -le "$max_try" ]; do
  code="$(curl -s -o /dev/null -w "%{http_code}" -X POST \
    -H "Content-Type: application/json" -d "$payload" "$base$endpoint" || true)"
  if [ "$code" = "200" ]; then
    printf '%s\n' "[PASS] external sync"
    exit 0
  fi
  i=$((i + 1))
  sleep 1
done

printf '%s\n' "[FAIL] external sync"
exit 1
