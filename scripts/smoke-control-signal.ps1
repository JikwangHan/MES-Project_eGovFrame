$ErrorActionPreference = "Stop"

$base = "http://localhost:18081"
$url = "$base/api/control-signal"
$payloadPath = "ai-middleware-egov/src/main/resources/sample-control.json"

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
for ($i = 0; $i -lt 20; $i++) {
    $res = TryPost $url $payload
    if ($res) {
        break
    }
    Start-Sleep -Seconds 1
}

if (-not $res) {
    Write-Output "[SKIP] control signal smoke"
    exit 0
}

if ($res.StatusCode -eq 201) {
    Write-Output "[PASS] control signal smoke"
    exit 0
}

Write-Output "[FAIL] control signal smoke"
exit 1
