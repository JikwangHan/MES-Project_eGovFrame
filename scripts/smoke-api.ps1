$ErrorActionPreference = "Stop"

function Assert-Status($resp, $expected) {
    if ($resp.StatusCode -ne $expected) {
        Write-Output "[FAIL] api status"
        exit 1
    }
}

$base = "http://localhost:18080"

$resp = Invoke-WebRequest -Uri "$base/health" -UseBasicParsing -TimeoutSec 5
Assert-Status $resp 200

$resp = Invoke-WebRequest -Method Post -Uri "$base/api/uplink" -UseBasicParsing -TimeoutSec 5 -ContentType "application/json" -Body "{}"
Assert-Status $resp 201

$resp = Invoke-WebRequest -Method Post -Uri "$base/api/direct-uplink" -UseBasicParsing -TimeoutSec 5 -ContentType "application/json" -Body "{}"
Assert-Status $resp 201

$resp = Invoke-WebRequest -Uri "$base/api/equipments?limit=1" -UseBasicParsing -TimeoutSec 5
Assert-Status $resp 200

$resp = Invoke-WebRequest -Uri "$base/api/equipments/EQ-001/telemetry?limit=1" -UseBasicParsing -TimeoutSec 5
Assert-Status $resp 200

$resp = Invoke-WebRequest -Uri "$base/api/dashboard/summary" -UseBasicParsing -TimeoutSec 5
Assert-Status $resp 200

Write-Output "[PASS] api smoke"
