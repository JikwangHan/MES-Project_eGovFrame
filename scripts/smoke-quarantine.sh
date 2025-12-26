#!/usr/bin/env sh
set -eu

# 목적: 격리 목록 조회 API가 동작하는지 확인한다.
# 이유: P1 단계에서 격리 경로가 최소한으로 검증되어야 한다.
base="http://localhost:18081"
tmp_out="${TMPDIR:-/tmp}/mes-mw-q.out"
tmp_err="${TMPDIR:-/tmp}/mes-mw-q.err"

mvn -s scripts/maven-settings.egov.xml -pl ai-middleware-egov -am package -q >/dev/null
mvn -s scripts/maven-settings.egov.xml -pl ai-middleware-egov exec:java -q >"$tmp_out" 2>"$tmp_err" &
pid=$!

cleanup() {
  if kill -0 "$pid" >/dev/null 2>&1; then
    kill -9 "$pid" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

code=""
max_try=10
i=1
while [ "$i" -le "$max_try" ]; do
  code="$(curl -s -o /dev/null -w "%{http_code}" "$base/api/quarantine" || true)"
  if [ "$code" = "200" ]; then
    printf '%s\n' "[PASS] quarantine smoke"
    exit 0
  fi
  i=$((i + 1))
  sleep 1
done

printf '%s\n' "[FAIL] quarantine smoke"
exit 1
