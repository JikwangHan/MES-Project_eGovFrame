$ErrorActionPreference = "Stop"

# 목적: /health 200 응답 여부만 확인한다.
# 이유: 가장 단순한 헬스 체크는 운영/스모크 자동화에 안정적이다.
try {
    $response = Invoke-WebRequest -Uri "http://localhost:18080/health" -UseBasicParsing -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Output "[PASS] health 200"
        exit 0
    }
    Write-Output "[FAIL] health not 200"
    exit 1
} catch {
    Write-Output "[FAIL] health request failed"
    exit 1
}
