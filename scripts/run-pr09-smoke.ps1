$ErrorActionPreference = "Stop"

$proc = Start-Process -FilePath "mvn" `
    -ArgumentList "-s scripts/maven-settings.egov.xml -pl ai-middleware-egov exec:java -q" `
    -PassThru -WindowStyle Hidden

Start-Sleep -Seconds 8

try {
    powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-iot-gateway.ps1
} finally {
    if ($proc -and -not $proc.HasExited) {
        Stop-Process -Id $proc.Id -Force
    }
}
