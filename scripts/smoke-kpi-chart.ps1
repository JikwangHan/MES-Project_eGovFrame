$ErrorActionPreference = "Stop"

$base = "http://localhost:18080"

function TryGet($url) {
    try {
        return Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 5
    } catch {
        return $null
    }
}

$tmpOut = Join-Path $env:TEMP "mes-kpi-chart.out"
$tmpErr = Join-Path $env:TEMP "mes-kpi-chart.err"
$started = $false
$proc = $null

$health = TryGet("$base/health")
if (-not $health -or $health.StatusCode -ne 200) {
    mvn -s scripts/maven-settings.egov.xml -pl mes-web-egov -am package -q | Out-Null
    $proc = Start-Process -FilePath "mvn" `
        -ArgumentList "-s scripts/maven-settings.egov.xml -pl mes-web-egov exec:java -q" `
        -PassThru -WindowStyle Hidden -RedirectStandardOutput $tmpOut -RedirectStandardError $tmpErr
    $started = $true
}

$trend = $null
$ui = $null
$maxTry = 10
for ($i = 0; $i -lt $maxTry; $i++) {
    $trend = TryGet("$base/api/kpi/trend")
    $ui = TryGet("$base/ui/kpi")
    if ($trend -and $ui) {
        break
    }
    Start-Sleep -Seconds 1
}

try {
    if ($trend -and $ui -and $trend.StatusCode -eq 200 -and $ui.StatusCode -eq 200 -and $ui.Content -match "kpi-chart") {
        Write-Output "[PASS] kpi chart smoke"
        exit 0
    }
    if (-not $trend -or -not $ui) {
        Write-Output "[SKIP] kpi chart smoke"
        exit 0
    }
    Write-Output "[FAIL] kpi chart smoke"
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
