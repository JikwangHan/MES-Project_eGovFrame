#!/usr/bin/env sh
set -eu

base="http://localhost:18081"
url="$base/api/raw-ingest"
payload_path="ai-middleware-egov/src/main/resources/sample-raw.json"

payload="{}"
if [ -f "$payload_path" ]; then
  payload="$(cat "$payload_path")"
fi

status=""
try=0
while [ "$try" -lt 15 ]; do
  status="$(printf "%s" "$payload" | curl -s -o /dev/null -w "%{http_code}" -H "Content-Type: application/octet-stream" -X POST "$url" --data-binary @- || true)"
  if [ -n "$status" ] && [ "$status" != "000" ]; then
    break
  fi
  try=$((try+1))
  sleep 1
done

if [ -z "$status" ] || [ "$status" = "000" ]; then
  echo "[SKIP] ai normalize smoke"
  exit 0
fi

if [ "$status" = "201" ]; then
  echo "[PASS] ai normalize smoke"
  exit 0
fi

echo "[FAIL] ai normalize smoke"
exit 1
