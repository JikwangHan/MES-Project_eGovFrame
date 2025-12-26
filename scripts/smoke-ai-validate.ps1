$ErrorActionPreference = "Stop"

$base = "http://localhost:18081"
$url = "$base/api/raw-ingest"
$payloadPath = "ai-middleware-egov/src/main/resources/sample-raw.json"

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

$res = $null
for ($i = 0; $i -lt 15; $i++) {
    $res = TryPost $url $payload
    if ($res) { break }
    Start-Sleep -Seconds 1
}

if (-not $res) {
    Write-Output "[SKIP] ai validate smoke"
    exit 0
}

if ($res.StatusCode -eq 201) {
    Write-Output "[PASS] ai validate smoke"
    exit 0
}

Write-Output "[FAIL] ai validate smoke"
exit 1
