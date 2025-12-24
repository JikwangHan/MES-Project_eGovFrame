$ErrorActionPreference = "Stop"

$files = @(
    "mes-contracts/src/main/resources/contracts/api-contracts.md",
    "mes-contracts/src/main/resources/contracts/error-codes.md",
    "mes-contracts/src/main/resources/contracts/signature-and-normalize.md"
)

foreach ($f in $files) {
    if (-not (Test-Path $f)) {
        Write-Output "[FAIL] contracts missing"
        exit 1
    }
}

Write-Output "[PASS] contracts ready"
