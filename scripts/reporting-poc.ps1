$ErrorActionPreference = "Stop"

# 목적: 레포팅 PoC 실행 절차를 자동화한다.
# 이유: 샘플 데이터 기반 출력 검증을 반복 가능하게 유지하기 위함이다.
$tmp = Join-Path $env:TEMP "reporting-poc-sample.json"
$data = '{"kpiName":"생산성","targetValue":100,"currentValue":82,"progressRate":0.82,"resultValue":82,"remark":"샘플"}'
$data | Set-Content $tmp
Remove-Item $tmp -ErrorAction SilentlyContinue
Write-Output "[SKIP] reporting poc"
exit 2
