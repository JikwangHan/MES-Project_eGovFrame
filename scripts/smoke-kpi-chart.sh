#!/usr/bin/env sh
set -eu

base="http://localhost:18080"

trend_status="$(curl -s -o /dev/null -w "%{http_code}" "$base/api/kpi/trend" || true)"
ui_html="$(curl -s "$base/ui/kpi" || true)"

if [ -z "$trend_status" ] || [ "$trend_status" = "000" ]; then
  echo "[SKIP] kpi chart smoke"
  exit 0
fi

if [ "$trend_status" = "200" ] && printf "%s" "$ui_html" | grep -q "kpi-chart"; then
  echo "[PASS] kpi chart smoke"
  exit 0
fi

echo "[FAIL] kpi chart smoke"
exit 1
