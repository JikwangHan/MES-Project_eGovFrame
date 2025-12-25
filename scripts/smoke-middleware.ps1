$ErrorActionPreference = "Stop"

# 목적: AI 미들웨어 원본 수신 API가 201로 동작하는지 확인한다.
# 이유: P0 단계에서는 원본 보관이 최우선이므로 성공 여부만 빠르게 판단한다.
$base = "http://localhost:18081"
$resp = Invoke-WebRequest -Method Post -Uri "$base/api/raw-ingest" -UseBasicParsing -TimeoutSec 5 -ContentType "text/plain" -Body "raw"

if ($resp.StatusCode -eq 201) {
    Write-Output "[PASS] middleware smoke"
    exit 0
}

Write-Output "[FAIL] middleware smoke"
exit 1
