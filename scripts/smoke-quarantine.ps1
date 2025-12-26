$ErrorActionPreference = "Stop"

# 목적: 격리 목록 조회 API가 동작하는지 확인한다.
# 이유: P1 단계에서 격리 경로가 최소한으로 검증되어야 한다.
$base = "http://localhost:18081"
$tmpOut = Join-Path $env:TEMP "mes-mw-q.out"
$tmpErr = Join-Path $env:TEMP "mes-mw-q.err"

mvn -s scripts/maven-settings.egov.xml -pl ai-middleware-egov -am package -q | Out-Null
$proc = Start-Process -FilePath "mvn" `
    -ArgumentList "-s scripts/maven-settings.egov.xml -pl ai-middleware-egov exec:java -q" `
    -PassThru -WindowStyle Hidden -RedirectStandardOutput $tmpOut -RedirectStandardError $tmpErr

try {
    $resp = $null
    $maxTry = 10
    for ($i = 0; $i -lt $maxTry; $i++) {
        try {
            $resp = Invoke-WebRequest -Uri "$base/api/quarantine" -UseBasicParsing -TimeoutSec 5
            break
        } catch {
            Start-Sleep -Seconds 1
        }
    }

    if ($resp -and $resp.StatusCode -eq 200) {
        Write-Output "[PASS] quarantine smoke"
        exit 0
    }
} finally {
    if ($proc -and -not $proc.HasExited) {
        Stop-Process -Id $proc.Id -Force
    }
}

Write-Output "[FAIL] quarantine smoke"
exit 1
