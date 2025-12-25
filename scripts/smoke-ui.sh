#!/usr/bin/env sh
set -eu

# 목적: UI 스캐폴딩 라우팅이 접근 가능한지 확인한다.
# 이유: 데이터 바인딩 전 단계에서 화면 골격이 준비됐는지 빠르게 확인하기 위함이다.
base="http://localhost:18080"
ui_path="/ui"
ui_sample_path="/ui/login"
ui_sample_path2="/ui/orders"
ui_sample_path3="/ui/equipment/status"
ui_sample_path4="/ui/dashboard/production"
ui_sample_path5="/ui/quality/defects/status"
ui_sample_path6="/ui/inventory/status"
ui_sample_path7="/ui/master/items"
tmp_out="${TMPDIR:-/tmp}/mes-ui.out"
tmp_err="${TMPDIR:-/tmp}/mes-ui.err"

# 먼저 서버가 떠 있는지 확인하고, 없다면 자동으로 실행한다.
# 이유: 스모크가 단독으로도 실행되도록 하기 위함이다.
started=0
health_code="$(curl -s -o /dev/null -w "%{http_code}" "$base/health" || true)"
if [ "$health_code" != "200" ]; then
  mvn -s scripts/maven-settings.egov.xml -pl mes-web-egov -am package -q >/dev/null
  mvn -s scripts/maven-settings.egov.xml -pl mes-web-egov exec:java -q >"$tmp_out" 2>"$tmp_err" &
  pid=$!
  started=1
fi

cleanup() {
  if [ "$started" -eq 1 ] && kill -0 "$pid" >/dev/null 2>&1; then
    kill -9 "$pid" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

ui_code=""
ui_sample_code=""
max_try=10
i=1
while [ "$i" -le "$max_try" ]; do
  ui_code="$(curl -s -o /dev/null -w "%{http_code}" "$base$ui_path" || true)"
  ui_sample_code="$(curl -s -o /dev/null -w "%{http_code}" "$base$ui_sample_path" || true)"
  ui_sample_code2="$(curl -s -o /dev/null -w "%{http_code}" "$base$ui_sample_path2" || true)"
  ui_sample_code3="$(curl -s -o /dev/null -w "%{http_code}" "$base$ui_sample_path3" || true)"
  ui_sample_code4="$(curl -s -o /dev/null -w "%{http_code}" "$base$ui_sample_path4" || true)"
  ui_sample_code5="$(curl -s -o /dev/null -w "%{http_code}" "$base$ui_sample_path5" || true)"
  ui_sample_code6="$(curl -s -o /dev/null -w "%{http_code}" "$base$ui_sample_path6" || true)"
  ui_sample_code7="$(curl -s -o /dev/null -w "%{http_code}" "$base$ui_sample_path7" || true)"
  if [ "$ui_code" = "200" ] && [ "$ui_sample_code" = "200" ] && [ "$ui_sample_code2" = "200" ] && [ "$ui_sample_code3" = "200" ] && [ "$ui_sample_code4" = "200" ] && [ "$ui_sample_code5" = "200" ] && [ "$ui_sample_code6" = "200" ] && [ "$ui_sample_code7" = "200" ]; then
    printf '%s\n' "[PASS] ui smoke"
    exit 0
  fi
  i=$((i + 1))
  sleep 1
done

printf '%s\n' "[FAIL] ui smoke"
exit 1
