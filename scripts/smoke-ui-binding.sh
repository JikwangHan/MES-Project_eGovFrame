#!/usr/bin/env sh
set -e

BASE="http://localhost:18080"
STARTED=0
PID=""

code=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/health" || true)
if [ "$code" != "200" ]; then
  # 서버가 없으면 자동으로 실행한다.
  # 이유: 스모크 스크립트는 단독 실행이 가능해야 한다.
  mvn -s scripts/maven-settings.egov.xml -pl mes-web-egov -am package -q >/dev/null 2>&1
  mvn -s scripts/maven-settings.egov.xml -pl mes-web-egov exec:java -q >/tmp/mes-ui-binding.out 2>/tmp/mes-ui-binding.err &
  PID=$!
  STARTED=1
fi

cleanup() {
  if [ "$STARTED" = "1" ] && [ -n "$PID" ]; then
    kill "$PID" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

check() {
  url="$1"
  code=$(curl -s -o /dev/null -w "%{http_code}" "$url" || true)
  [ "$code" = "200" ]
}

tries=0
while [ "$tries" -lt 10 ]; do
  if check "$BASE/api/dashboard/summary" \
    && check "$BASE/api/equipments" \
    && check "$BASE/api/orders" \
    && check "$BASE/api/jobs" \
    && check "$BASE/api/kpi" \
    && check "$BASE/api/kpi/trend" \
    && check "$BASE/ui/dashboard/production" \
    && check "$BASE/ui/equipment/status" \
    && check "$BASE/ui/orders" \
    && check "$BASE/ui/jobs" \
    && check "$BASE/ui/kpi"; then
    echo "[PASS] ui binding smoke"
    exit 0
  fi
  tries=$((tries + 1))
  sleep 1
done

if [ "$STARTED" = "1" ]; then
  echo "[FAIL] ui binding smoke"
  exit 1
fi

if check "$BASE/api/dashboard/summary" \
  && check "$BASE/api/equipments" \
  && check "$BASE/api/orders" \
  && check "$BASE/api/jobs" \
  && check "$BASE/api/kpi" \
  && check "$BASE/api/kpi/trend" \
  && check "$BASE/ui/dashboard/production" \
  && check "$BASE/ui/equipment/status" \
  && check "$BASE/ui/orders" \
  && check "$BASE/ui/jobs" \
  && check "$BASE/ui/kpi"; then
  echo "[PASS] ui binding smoke"
  exit 0
fi

echo "[FAIL] ui binding smoke"
exit 1
