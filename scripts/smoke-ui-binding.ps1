$ErrorActionPreference = "Stop"

$base = "http://localhost:18080"

function TryGet($url) {
    try {
        return Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 5
    } catch {
        return $null
    }
}

$tmpOut = Join-Path $env:TEMP "mes-ui-binding.out"
$tmpErr = Join-Path $env:TEMP "mes-ui-binding.err"
$started = $false
$proc = $null

$health = TryGet("$base/health")
if (-not $health -or $health.StatusCode -ne 200) {
    # 서버가 없으면 자동으로 실행한다.
    # 이유: 스모크 스크립트는 단독 실행이 가능해야 한다.
    mvn -s scripts/maven-settings.egov.xml -pl mes-web-egov -am package -q | Out-Null
    $proc = Start-Process -FilePath "mvn" `
        -ArgumentList "-s scripts/maven-settings.egov.xml -pl mes-web-egov exec:java -q" `
        -PassThru -WindowStyle Hidden -RedirectStandardOutput $tmpOut -RedirectStandardError $tmpErr
    $started = $true
}

$dash = $null
$equip = $null
$orders = $null
$jobs = $null
$kpi = $null
$kpiTrend = $null
$uiDash = $null
$uiEquip = $null
$uiOrders = $null
$uiJobs = $null
$uiKpi = $null
$maxTry = 10
for ($i = 0; $i -lt $maxTry; $i++) {
    $dash = TryGet("$base/api/dashboard/summary")
    $equip = TryGet("$base/api/equipments")
    $orders = TryGet("$base/api/orders")
    $jobs = TryGet("$base/api/jobs")
    $kpi = TryGet("$base/api/kpi")
    $kpiTrend = TryGet("$base/api/kpi/trend")
    $uiDash = TryGet("$base/ui/dashboard/production")
    $uiEquip = TryGet("$base/ui/equipment/status")
    $uiOrders = TryGet("$base/ui/orders")
    $uiJobs = TryGet("$base/ui/jobs")
    $uiKpi = TryGet("$base/ui/kpi")
    if ($dash -and $uiDash) {
        break
    }
    Start-Sleep -Seconds 1
}

$telemetryOk = $true
if ($equip -and $equip.StatusCode -eq 200) {
    try {
        $payload = $equip.Content | ConvertFrom-Json
    } catch {
        $payload = $null
    }
    if ($payload -and $payload.data -and $payload.data.Count -gt 0) {
        $deviceId = $payload.data[0].deviceId
        if ($deviceId) {
            $telemetry = TryGet("$base/api/equipments/$deviceId/telemetry")
            if (-not $telemetry -or $telemetry.StatusCode -ne 200) {
                $telemetryOk = $false
            }
        }
    }
}

try {
    if ($dash -and $equip -and $orders -and $jobs -and $kpi -and $kpiTrend -and $uiDash -and $uiEquip -and $uiOrders -and $uiJobs -and $uiKpi -and $dash.StatusCode -eq 200 -and $equip.StatusCode -eq 200 -and $orders.StatusCode -eq 200 -and $jobs.StatusCode -eq 200 -and $kpi.StatusCode -eq 200 -and $kpiTrend.StatusCode -eq 200 -and $uiDash.StatusCode -eq 200 -and $uiEquip.StatusCode -eq 200 -and $uiOrders.StatusCode -eq 200 -and $uiJobs.StatusCode -eq 200 -and $uiKpi.StatusCode -eq 200 -and $telemetryOk) {
        Write-Output "[PASS] ui binding smoke"
        exit 0
    }

    Write-Output "[FAIL] ui binding smoke"
    exit 1
} finally {
    if ($started) {
        if ($proc -and -not $proc.HasExited) {
            Stop-Process -Id $proc.Id -Force
        }
        # 안전하게 포트 점유 프로세스도 정리한다.
        try {
            $conn = Get-NetTCPConnection -LocalPort 18080 -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
            if ($conn) {
                Stop-Process -Id $conn.OwningProcess -Force
            }
        } catch {
            # 실패 시에도 스크립트 결과는 유지한다.
        }
    }
}
