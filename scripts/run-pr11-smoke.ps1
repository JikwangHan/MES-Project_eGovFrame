$ErrorActionPreference = "Stop"

$proc = Start-Process -FilePath "mvn" `
    -ArgumentList "-s scripts/maven-settings.egov.xml -pl ai-middleware-egov exec:java -q" `
    -PassThru -WindowStyle Hidden

function TryGet($uri) {
    try {
        return Invoke-WebRequest -Uri $uri -UseBasicParsing -TimeoutSec 3
    } catch {
        return $null
    }
}

for ($i = 0; $i -lt 30; $i++) {
    $health = TryGet "http://localhost:18081/health"
    if ($health -and $health.StatusCode -eq 200) {
        break
    }
    Start-Sleep -Seconds 1
}

try {
    powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-ai-normalize.ps1
} finally {
    if ($proc -and -not $proc.HasExited) {
        Stop-Process -Id $proc.Id -Force
    }
}
