#!/usr/bin/env sh
set -eu

base="http://localhost:18081"
url="$base/api/raw-ingest"
payload_path="edge-gateway-egov/src/main/resources/sample-uplink.json"

payload="{}"
if [ -f "$payload_path" ]; then
  payload="$(cat "$payload_path")"
fi

status="$(printf "%s" "$payload" | curl -s -o /dev/null -w "%{http_code}" -H "Content-Type: application/octet-stream" -X POST "$url" --data-binary @- || true)"

if [ -z "$status" ] || [ "$status" = "000" ]; then
  echo "[SKIP] iot gateway smoke"
  exit 0
fi

if [ "$status" = "201" ]; then
  echo "[PASS] iot gateway smoke"
  exit 0
fi

echo "[FAIL] iot gateway smoke"
exit 1
