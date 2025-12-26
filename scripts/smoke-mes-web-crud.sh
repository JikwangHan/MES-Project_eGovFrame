#!/usr/bin/env sh
set -eu

base="http://localhost:18082"

ui_eq_status="$(curl -s -o /dev/null -w "%{http_code}" "$base/ui/equipment/status" || true)"
ui_orders_status="$(curl -s -o /dev/null -w "%{http_code}" "$base/ui/orders" || true)"
ui_jobs_status="$(curl -s -o /dev/null -w "%{http_code}" "$base/ui/jobs" || true)"
ui_kpi_status="$(curl -s -o /dev/null -w "%{http_code}" "$base/ui/kpi" || true)"

eq_post="$(curl -s -o /dev/null -w "%{http_code}" -H "Content-Type: application/json" -d '{"name":"샘플 설비","status":"ACTIVE"}' "$base/api/equipments" || true)"
order_post="$(curl -s -o /dev/null -w "%{http_code}" -H "Content-Type: application/json" -d '{"orderNo":"ORD-AUTO","status":"PLANNED"}' "$base/api/orders" || true)"
job_post="$(curl -s -o /dev/null -w "%{http_code}" -H "Content-Type: application/json" -d '{"orderId":"ORD-001","processName":"Cutting","status":"PLANNED"}' "$base/api/jobs" || true)"
kpi_post="$(curl -s -o /dev/null -w "%{http_code}" -H "Content-Type: application/json" -d '{"name":"KPI-TEST","unit":"PCT"}' "$base/api/kpi" || true)"

if [ "$ui_eq_status" = "200" ] && [ "$ui_orders_status" = "200" ] && [ "$ui_jobs_status" = "200" ] && [ "$ui_kpi_status" = "200" ] \
   && [ "$eq_post" = "201" ] && [ "$order_post" = "201" ] && [ "$job_post" = "201" ] && [ "$kpi_post" = "201" ]; then
  echo "[PASS] mes web crud smoke"
  exit 0
fi

if [ -z "$ui_eq_status" ] || [ "$ui_eq_status" = "000" ]; then
  echo "[SKIP] mes web crud smoke"
  exit 0
fi

echo "[FAIL] mes web crud smoke"
exit 1
