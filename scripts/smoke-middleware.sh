#!/usr/bin/env sh
set -eu

# 목적: AI 미들웨어가 기동되고 원본 수신 API가 201을 반환하는지 확인한다.
# 이유: P0 단계에서는 원본 보관 성공 여부가 가장 중요한 PASS 기준이기 때문이다.
base="http://localhost:18081"
tmp_out="${TMPDIR:-/tmp}/mes-mw.out"
tmp_err="${TMPDIR:-/tmp}/mes-mw.err"

# 먼저 필요한 모듈만 빌드해 실행 준비를 맞춘다.
mvn -s scripts/maven-settings.egov.xml -pl ai-middleware-egov -am package -q >/dev/null

# 미들웨어를 백그라운드로 실행한다.
# 이유: 스모크 스크립트가 API 호출까지 이어서 수행해야 하기 때문이다.
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
  code="$(curl -s -o /dev/null -w "%{http_code}" -X POST "$base/api/raw-ingest" -H "Content-Type: text/plain" -d "raw" || true)"
  if [ "$code" = "201" ]; then
    printf '%s\n' "[PASS] middleware smoke"
    exit 0
  fi
  i=$((i + 1))
  sleep 1
done

printf '%s\n' "[FAIL] middleware smoke"
exit 1
