$ErrorActionPreference = "Stop"
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/health" -UseBasicParsing -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Output "[PASS] health 200"
        exit 0
    }
    Write-Output "[FAIL] health not 200"
    exit 1
} catch {
    Write-Output "[FAIL] health request failed"
    exit 1
}
