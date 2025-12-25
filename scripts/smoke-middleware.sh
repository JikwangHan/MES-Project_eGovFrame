#!/usr/bin/env sh
set -eu

# 목적: AI 미들웨어 원본 수신 API가 201로 동작하는지 확인한다.
# 이유: P0 단계에서는 원본 보관이 최우선이므로 성공 여부만 빠르게 판단한다.
base="http://localhost:18081"
code="$(curl -s -o /dev/null -w "%{http_code}" -X POST "$base/api/raw-ingest" -H "Content-Type: text/plain" -d "raw" || true)"

[ "$code" = "201" ] && { printf '%s\n' "[PASS] middleware smoke"; exit 0; }
printf '%s\n' "[FAIL] middleware smoke"
exit 1
