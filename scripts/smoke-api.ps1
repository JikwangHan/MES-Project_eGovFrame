$ErrorActionPreference = "Stop"

# 목적: PR-02 최소 API가 200/201로 응답하는지 확인한다.
# 이유: API 기본 동작이 깨지면 이후 모듈 연동이 불가능해지기 때문이다.

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
