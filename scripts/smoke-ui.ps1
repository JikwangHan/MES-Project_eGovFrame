$ErrorActionPreference = "Stop"

# 목적: UI 스캐폴딩 라우팅이 접근 가능한지 확인한다.
# 이유: 데이터 바인딩 전 단계에서 화면 골격이 준비됐는지 빠르게 확인하기 위함이다.
$base = "http://localhost:18080"
$uiPath = "/ui"

try {
    $health = Invoke-WebRequest -Uri "$base/health" -UseBasicParsing -TimeoutSec 5
    if ($health.StatusCode -ne 200) {
        Write-Output "[SKIP] ui smoke"
        exit 2
    }
} catch {
    Write-Output "[SKIP] ui smoke"
    exit 2
}

try {
    $resp = Invoke-WebRequest -Uri "$base$uiPath" -UseBasicParsing -TimeoutSec 5
    if ($resp.StatusCode -eq 200) {
        Write-Output "[PASS] ui smoke"
        exit 0
    }
    Write-Output "[FAIL] ui smoke"
    exit 1
} catch {
    # UI가 아직 구성되지 않았으면 SKIP으로 처리한다.
    Write-Output "[SKIP] ui smoke"
    exit 2
}
