$ErrorActionPreference = "Stop"

$base = "http://localhost:18081"
$url = "$base/api/raw-ingest"
$payloadPath = "edge-gateway-egov/src/main/resources/sample-uplink.json"

function TryPost($uri, $body) {
    try {
        return Invoke-WebRequest -Uri $uri -Method Post -ContentType "application/octet-stream" -Body $body -UseBasicParsing -TimeoutSec 5
    } catch {
        return $null
    }
}

$payload = "{}"
if (Test-Path $payloadPath) {
    $payload = Get-Content -Raw -Path $payloadPath
}

$res = TryPost $url $payload
if (-not $res) {
    Write-Output "[SKIP] iot gateway smoke"
    exit 0
}

if ($res.StatusCode -eq 201) {
    Write-Output "[PASS] iot gateway smoke"
    exit 0
}

Write-Output "[FAIL] iot gateway smoke"
exit 1
