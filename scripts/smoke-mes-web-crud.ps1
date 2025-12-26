$ErrorActionPreference = "Stop"

$base = "http://localhost:18082"

function TryGet($url) {
    try {
        return Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 5
    } catch {
        return $null
    }
}

function TryPost($url, $body) {
    try {
        return Invoke-WebRequest -Uri $url -Method Post -ContentType "application/json" -Body $body -UseBasicParsing -TimeoutSec 5
    } catch {
        return $null
    }
}

$tmpOut = Join-Path $env:TEMP "mes-web-crud.out"
$tmpErr = Join-Path $env:TEMP "mes-web-crud.err"
$started = $false
$proc = $null

$health = TryGet("$base/health")
if (-not $health -or $health.StatusCode -ne 200) {
    mvn -s scripts/maven-settings.egov.xml -pl mes-web-egov -am package -q | Out-Null
    $proc = Start-Process -FilePath "mvn" `
        -ArgumentList "-s scripts/maven-settings.egov.xml -pl mes-web-egov exec:java -q -Dserver.port=18082" `
        -PassThru -WindowStyle Hidden -RedirectStandardOutput $tmpOut -RedirectStandardError $tmpErr
    $started = $true
    for ($i = 0; $i -lt 15; $i++) {
        $health = TryGet("$base/health")
        if ($health -and $health.StatusCode -eq 200) {
            break
        }
        Start-Sleep -Seconds 1
    }
}

$uiEq = $null
$uiOrders = $null
$uiJobs = $null
$uiKpi = $null
for ($i = 0; $i -lt 15; $i++) {
    $uiEq = TryGet("$base/ui/equipment/status")
    $uiOrders = TryGet("$base/ui/orders")
    $uiJobs = TryGet("$base/ui/jobs")
    $uiKpi = TryGet("$base/ui/kpi")
    if ($uiEq -and $uiOrders -and $uiJobs -and $uiKpi) {
        break
    }
    Start-Sleep -Seconds 1
}

$eqBody = '{"name":"샘플 설비","status":"ACTIVE"}'
$orderBody = '{"orderNo":"ORD-AUTO","status":"PLANNED"}'
$jobBody = '{"orderId":"ORD-001","processName":"Cutting","status":"PLANNED"}'
$kpiBody = '{"name":"KPI-TEST","unit":"PCT"}'

$eqPost = $null
$orderPost = $null
$jobPost = $null
$kpiPost = $null
for ($i = 0; $i -lt 10; $i++) {
    $eqPost = TryPost("$base/api/equipments", $eqBody)
    $orderPost = TryPost("$base/api/orders", $orderBody)
    $jobPost = TryPost("$base/api/jobs", $jobBody)
    $kpiPost = TryPost("$base/api/kpi", $kpiBody)
    if ($eqPost -and $orderPost -and $jobPost -and $kpiPost) {
        break
    }
    Start-Sleep -Seconds 1
}

try {
    $uiEqStatus = if ($uiEq) { $uiEq.StatusCode } else { 0 }
    $uiOrdersStatus = if ($uiOrders) { $uiOrders.StatusCode } else { 0 }
    $uiJobsStatus = if ($uiJobs) { $uiJobs.StatusCode } else { 0 }
    $uiKpiStatus = if ($uiKpi) { $uiKpi.StatusCode } else { 0 }
    $eqPostStatus = if ($eqPost) { $eqPost.StatusCode } else { 0 }
    $orderPostStatus = if ($orderPost) { $orderPost.StatusCode } else { 0 }
    $jobPostStatus = if ($jobPost) { $jobPost.StatusCode } else { 0 }
    $kpiPostStatus = if ($kpiPost) { $kpiPost.StatusCode } else { 0 }

    if ($uiEqStatus -eq 200 -and $uiOrdersStatus -eq 200 `
        -and $uiJobsStatus -eq 200 -and $uiKpiStatus -eq 200 `
        -and $eqPostStatus -eq 201 -and $orderPostStatus -eq 201 `
        -and $jobPostStatus -eq 201 -and $kpiPostStatus -eq 201) {
        Write-Output "[PASS] mes web crud smoke"
        exit 0
    }
    if ($uiEqStatus -eq 0 -or $uiOrdersStatus -eq 0 -or $uiJobsStatus -eq 0 -or $uiKpiStatus -eq 0) {
        Write-Output "[SKIP] mes web crud smoke"
        exit 0
    }
    Write-Output "[FAIL] mes web crud smoke"
    exit 1
} finally {
    if ($started) {
        if ($proc -and -not $proc.HasExited) {
            Stop-Process -Id $proc.Id -Force
        }
        try {
            $conn = Get-NetTCPConnection -LocalPort 18080 -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
            if ($conn) {
                Stop-Process -Id $conn.OwningProcess -Force
            }
        } catch {
        }
    }
}
