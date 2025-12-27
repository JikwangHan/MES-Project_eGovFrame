$ErrorActionPreference = "Stop"

# 목적: 외부기관 연계가 최소한의 응답을 반환하는지 확인한다.
# 이유: 연계 API 확정 전이라도 스모크 기준을 유지하기 위함이다.
$base = $env:MES_WEB_BASE_URL
if ([string]::IsNullOrWhiteSpace($base)) {
    $base = "http://localhost:18082"
}
$endpoint = "/api/external-sync"
$maxTry = 3
$fromDate = (Get-Date).AddDays(-7).ToString("yyyy-MM-dd")
$toDate = (Get-Date).ToString("yyyy-MM-dd")
$payload = @{ from = $fromDate; to = $toDate } | ConvertTo-Json

try {
    $health = Invoke-WebRequest -Uri "$base/health" -UseBasicParsing -TimeoutSec 5
    if ($health.StatusCode -ne 200) {
        Write-Output "[SKIP] external sync"
        exit 2
    }
    for ($i = 0; $i -lt $maxTry; $i++) {
        try {
            $resp = Invoke-WebRequest -Method Post -Uri "$base$endpoint" -UseBasicParsing -TimeoutSec 5 `
                -ContentType "application/json" -Body $payload
            if ($resp.StatusCode -eq 200) {
                Write-Output "[PASS] external sync"
                exit 0
            }
        } catch {
            Start-Sleep -Seconds 1
        }
    }
    Write-Output "[FAIL] external sync"
    exit 1
} catch {
    Write-Output "[SKIP] external sync"
    exit 2
}
