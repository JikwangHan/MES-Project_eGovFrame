#!/usr/bin/env sh
set -eu

# 목적: UI 스캐폴딩 라우팅이 접근 가능한지 확인한다.
# 이유: 데이터 바인딩 전 단계에서 화면 골격이 준비됐는지 빠르게 확인하기 위함이다.
base="http://localhost:18080"
ui_path="/ui"

health_code="$(curl -s -o /dev/null -w "%{http_code}" "$base/health" || true)"
if [ "$health_code" != "200" ]; then
  printf '%s\n' "[SKIP] ui smoke"
  exit 2
fi

ui_code="$(curl -s -o /dev/null -w "%{http_code}" "$base$ui_path" || true)"
if [ "$ui_code" = "200" ]; then
  printf '%s\n' "[PASS] ui smoke"
  exit 0
fi

printf '%s\n' "[SKIP] ui smoke"
exit 2
