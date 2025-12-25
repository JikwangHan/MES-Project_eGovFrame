$ErrorActionPreference = "Stop"

# 목적: 게이트웨이 시뮬레이터가 /api/uplink로 201을 받는지 확인한다.
# 이유: PR-03 최소 연동 흐름의 성공 기준을 명확히 하기 위함이다.

# MES Web 서비스가 먼저 실행되어 있어야 한다.
mvn -s scripts/maven-settings.egov.xml -pl edge-gateway-egov -am package -q | Out-Null
$out = mvn -s scripts/maven-settings.egov.xml -pl edge-gateway-egov exec:java -q 2>&1

if ($out.Contains("[PASS]")) {
    Write-Output "[PASS] gateway smoke"
    exit 0
}

Write-Output "[FAIL] gateway smoke"
exit 1
