$ErrorActionPreference = "Stop"

# 목적: AI 미들웨어가 기동되고 원본 수신 API가 201을 반환하는지 확인한다.
# 이유: P0 단계에서는 원본 보관 성공 여부가 가장 중요한 PASS 기준이기 때문이다.
$base = "http://localhost:18081"
$tmpOut = Join-Path $env:TEMP "mes-mw.out"
$tmpErr = Join-Path $env:TEMP "mes-mw.err"

# 먼저 필요한 모듈만 빌드해 실행 준비를 맞춘다.
mvn -s scripts/maven-settings.egov.xml -pl ai-middleware-egov -am package -q | Out-Null

# 미들웨어를 백그라운드로 실행한다.
# 이유: 스모크 스크립트가 API 호출까지 이어서 수행해야 하기 때문이다.
$proc = Start-Process -FilePath "mvn" `
    -ArgumentList "-s scripts/maven-settings.egov.xml -pl ai-middleware-egov exec:java -q" `
    -PassThru -WindowStyle Hidden -RedirectStandardOutput $tmpOut -RedirectStandardError $tmpErr

try {
    $resp = $null
    $maxTry = 10
    for ($i = 0; $i -lt $maxTry; $i++) {
        try {
            # 기동 직후 접속 실패가 날 수 있어 재시도한다.
            $resp = Invoke-WebRequest -Method Post -Uri "$base/api/raw-ingest" -UseBasicParsing -TimeoutSec 5 -ContentType "text/plain" -Body "raw"
            break
        } catch {
            Start-Sleep -Seconds 1
        }
    }

    if ($resp -and $resp.StatusCode -eq 201) {
        Write-Output "[PASS] middleware smoke"
        exit 0
    }
} finally {
    # 미들웨어를 종료해 포트를 정리한다.
    if ($proc -and -not $proc.HasExited) {
        Stop-Process -Id $proc.Id -Force
    }
}

Write-Output "[FAIL] middleware smoke"
exit 1
