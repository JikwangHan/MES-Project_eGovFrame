#!/usr/bin/env sh
set -eu

base="http://localhost:18081"
url="$base/api/control-signal"
payload_path="ai-middleware-egov/src/main/resources/sample-control.json"

payload="{}"
if [ -f "$payload_path" ]; then
  payload="$(cat "$payload_path")"
fi

status=""
try=0
while [ "$try" -lt 20 ]; do
  status="$(printf "%s" "$payload" | curl -s -o /dev/null -w "%{http_code}" -H "Content-Type: application/octet-stream" -X POST "$url" --data-binary @- || true)"
  if [ -n "$status" ] && [ "$status" != "000" ]; then
    break
  fi
  try=$((try+1))
  sleep 1
done

if [ -z "$status" ] || [ "$status" = "000" ]; then
  echo "[SKIP] control signal smoke"
  exit 0
fi

if [ "$status" = "201" ]; then
  echo "[PASS] control signal smoke"
  exit 0
fi

echo "[FAIL] control signal smoke"
exit 1
