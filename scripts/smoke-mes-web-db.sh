#!/usr/bin/env sh

# 목적: DB 연동 모드에서 기본 CRUD API가 응답 가능한지 빠르게 확인한다.
# 이유: PR-16의 PASS 기준(연결 성공 + CRUD 동작)을 스모크로 보증하기 위함이다.

if [ -z "$MES_DB_URL" ] || [ -z "$MES_DB_USER" ] || [ -z "$MES_DB_PASSWORD" ]; then
  echo "[SKIP] DB env not set"
  exit 0
fi

BASE_URL="${MES_WEB_BASE_URL:-http://localhost:18082}"

check_api() {
  url="$1"
  code="$(curl -s -o /dev/null -w "%{http_code}" "$url")"
  if [ "$code" != "200" ]; then
    echo "[FAIL] $url failed"
    exit 1
  fi
}

check_api "$BASE_URL/api/equipments"
check_api "$BASE_URL/api/orders"
check_api "$BASE_URL/api/jobs"
check_api "$BASE_URL/api/kpi"

echo "[PASS] db smoke ok"
