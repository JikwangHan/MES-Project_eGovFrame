$ErrorActionPreference = "Stop"

# 목적: 계약 문서 파일이 존재하는지 확인한다.
# 이유: 계약이 없으면 이후 구현/스모크 기준이 흔들리므로 즉시 실패 처리한다.

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
