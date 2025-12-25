#!/usr/bin/env sh
set -eu

# 목적: PR-02 최소 API가 200/201으로 응답하는지 확인한다.
# 이유: API 기본 동작이 깨지면 연동 테스트가 의미가 없다.

base="http://localhost:18080"

code="$(curl -s -o /dev/null -w "%{http_code}" "$base/health" || true)"
[ "$code" = "200" ] || { printf '%s\n' "[FAIL] api status"; exit 1; }

code="$(curl -s -o /dev/null -w "%{http_code}" -X POST "$base/api/uplink" -H "Content-Type: application/json" -d "{}" || true)"
[ "$code" = "201" ] || { printf '%s\n' "[FAIL] api status"; exit 1; }

code="$(curl -s -o /dev/null -w "%{http_code}" -X POST "$base/api/direct-uplink" -H "Content-Type: application/json" -d "{}" || true)"
[ "$code" = "201" ] || { printf '%s\n' "[FAIL] api status"; exit 1; }

code="$(curl -s -o /dev/null -w "%{http_code}" "$base/api/equipments?limit=1" || true)"
[ "$code" = "200" ] || { printf '%s\n' "[FAIL] api status"; exit 1; }

code="$(curl -s -o /dev/null -w "%{http_code}" "$base/api/equipments/EQ-001/telemetry?limit=1" || true)"
[ "$code" = "200" ] || { printf '%s\n' "[FAIL] api status"; exit 1; }

code="$(curl -s -o /dev/null -w "%{http_code}" "$base/api/dashboard/summary" || true)"
[ "$code" = "200" ] || { printf '%s\n' "[FAIL] api status"; exit 1; }

printf '%s\n' "[PASS] api smoke"
