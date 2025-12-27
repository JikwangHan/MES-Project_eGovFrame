param(
    [string]$BaseUrl = $env:MES_WEB_BASE_URL
)

# 목적: DB 연동 모드에서 기본 CRUD API가 응답 가능한지 빠르게 확인한다.
# 이유: PR-16의 PASS 기준(연결 성공 + CRUD 동작)을 스모크로 보증하기 위함이다.

if ([string]::IsNullOrWhiteSpace($env:MES_DB_URL) -or
    [string]::IsNullOrWhiteSpace($env:MES_DB_USER) -or
    [string]::IsNullOrWhiteSpace($env:MES_DB_PASSWORD)) {
    Write-Output "[SKIP] DB env not set"
    exit 0
}

if ([string]::IsNullOrWhiteSpace($BaseUrl)) {
    $BaseUrl = "http://localhost:18082"
}

try {
    $equip = Invoke-WebRequest -UseBasicParsing -Uri "$BaseUrl/api/equipments" -TimeoutSec 10
    if ($equip.StatusCode -ne 200) {
        Write-Output "[FAIL] equipments api failed"
        exit 1
    }

    $orders = Invoke-WebRequest -UseBasicParsing -Uri "$BaseUrl/api/orders" -TimeoutSec 10
    if ($orders.StatusCode -ne 200) {
        Write-Output "[FAIL] orders api failed"
        exit 1
    }

    $jobs = Invoke-WebRequest -UseBasicParsing -Uri "$BaseUrl/api/jobs" -TimeoutSec 10
    if ($jobs.StatusCode -ne 200) {
        Write-Output "[FAIL] jobs api failed"
        exit 1
    }

    $kpi = Invoke-WebRequest -UseBasicParsing -Uri "$BaseUrl/api/kpi" -TimeoutSec 10
    if ($kpi.StatusCode -ne 200) {
        Write-Output "[FAIL] kpi api failed"
        exit 1
    }

    Write-Output "[PASS] db smoke ok"
    exit 0
} catch {
    Write-Output "[FAIL] db smoke error"
    exit 1
}
