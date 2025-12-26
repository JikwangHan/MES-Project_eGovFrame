$ErrorActionPreference = "Stop"

$base = "http://localhost:18082"

$proc = Start-Process -FilePath "mvn" `
    -ArgumentList "-s scripts/maven-settings.egov.xml -pl mes-web-egov exec:java -q -Dserver.port=18082" `
    -PassThru -WindowStyle Hidden

for ($i = 0; $i -lt 20; $i++) {
    try {
        $health = Invoke-WebRequest -Uri "$base/health" -UseBasicParsing -TimeoutSec 5
        if ($health.StatusCode -eq 200) {
            break
        }
    } catch {
    }
    Start-Sleep -Seconds 1
}

try {
    $uiEq = Invoke-WebRequest -UseBasicParsing -Uri "$base/ui/equipment/status" -TimeoutSec 5
    $uiOrders = Invoke-WebRequest -UseBasicParsing -Uri "$base/ui/orders" -TimeoutSec 5
    $uiJobs = Invoke-WebRequest -UseBasicParsing -Uri "$base/ui/jobs" -TimeoutSec 5
    $uiKpi = Invoke-WebRequest -UseBasicParsing -Uri "$base/ui/kpi" -TimeoutSec 5

    if ($uiEq.StatusCode -ne 200 -or $uiOrders.StatusCode -ne 200 `
        -or $uiJobs.StatusCode -ne 200 -or $uiKpi.StatusCode -ne 200) {
        Write-Output "[FAIL] mes web crud smoke"
        exit 1
    }

    $eqPost = Invoke-WebRequest -UseBasicParsing -Method Post -ContentType "application/json" `
        -Body '{"name":"샘플 설비","status":"ACTIVE"}' -Uri "$base/api/equipments" -TimeoutSec 5
    $orderPost = Invoke-WebRequest -UseBasicParsing -Method Post -ContentType "application/json" `
        -Body '{"orderNo":"ORD-AUTO","status":"PLANNED"}' -Uri "$base/api/orders" -TimeoutSec 5
    $jobPost = Invoke-WebRequest -UseBasicParsing -Method Post -ContentType "application/json" `
        -Body '{"orderId":"ORD-001","processName":"Cutting","status":"PLANNED"}' -Uri "$base/api/jobs" -TimeoutSec 5
    $kpiPost = Invoke-WebRequest -UseBasicParsing -Method Post -ContentType "application/json" `
        -Body '{"name":"KPI-TEST","unit":"PCT"}' -Uri "$base/api/kpi" -TimeoutSec 5

    if ($eqPost.StatusCode -eq 201 -and $orderPost.StatusCode -eq 201 `
        -and $jobPost.StatusCode -eq 201 -and $kpiPost.StatusCode -eq 201) {
        Write-Output "[PASS] mes web crud smoke"
        exit 0
    }
    Write-Output "[FAIL] mes web crud smoke"
    exit 1
} finally {
    if ($proc -and -not $proc.HasExited) {
        Stop-Process -Id $proc.Id -Force
    }
}
