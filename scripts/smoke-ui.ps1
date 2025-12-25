$ErrorActionPreference = "Stop"

# 목적: UI 스캐폴딩 라우팅이 접근 가능한지 확인한다.
# 이유: 데이터 바인딩 전 단계에서 화면 골격이 준비됐는지 빠르게 확인하기 위함이다.
$base = "http://localhost:18080"
$uiPath = "/ui"
$uiSamplePath = "/ui/login"
$uiSamplePath2 = "/ui/orders"
$uiSamplePath3 = "/ui/equipment/status"
$uiSamplePath4 = "/ui/dashboard/production"
$tmpOut = Join-Path $env:TEMP "mes-ui.out"
$tmpErr = Join-Path $env:TEMP "mes-ui.err"

# 먼저 서버가 떠 있는지 확인하고, 없다면 자동으로 실행한다.
# 이유: 스모크가 단독으로도 실행되도록 하기 위함이다.
$started = $false
$proc = $null
try {
    try {
        $health = Invoke-WebRequest -Uri "$base/health" -UseBasicParsing -TimeoutSec 3
    } catch {
        $health = $null
    }

    if (-not $health -or $health.StatusCode -ne 200) {
        mvn -s scripts/maven-settings.egov.xml -pl mes-web-egov -am package -q | Out-Null
        $proc = Start-Process -FilePath "mvn" `
            -ArgumentList "-s scripts/maven-settings.egov.xml -pl mes-web-egov exec:java -q" `
            -PassThru -WindowStyle Hidden -RedirectStandardOutput $tmpOut -RedirectStandardError $tmpErr
        $started = $true
    }

    $resp = $null
    $resp2 = $null
    $resp3 = $null
    $resp4 = $null
    $resp5 = $null
    $maxTry = 10
    for ($i = 0; $i -lt $maxTry; $i++) {
        try {
            $resp = Invoke-WebRequest -Uri "$base$uiPath" -UseBasicParsing -TimeoutSec 5
            $resp2 = Invoke-WebRequest -Uri "$base$uiSamplePath" -UseBasicParsing -TimeoutSec 5
            $resp3 = Invoke-WebRequest -Uri "$base$uiSamplePath2" -UseBasicParsing -TimeoutSec 5
            $resp4 = Invoke-WebRequest -Uri "$base$uiSamplePath3" -UseBasicParsing -TimeoutSec 5
            $resp5 = Invoke-WebRequest -Uri "$base$uiSamplePath4" -UseBasicParsing -TimeoutSec 5
            break
        } catch {
            Start-Sleep -Seconds 1
        }
    }

    if ($resp -and $resp2 -and $resp3 -and $resp4 -and $resp5 -and $resp.StatusCode -eq 200 -and $resp2.StatusCode -eq 200 -and $resp3.StatusCode -eq 200 -and $resp4.StatusCode -eq 200 -and $resp5.StatusCode -eq 200) {
        Write-Output "[PASS] ui smoke"
        exit 0
    }

    Write-Output "[FAIL] ui smoke"
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
