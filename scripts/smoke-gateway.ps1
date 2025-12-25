$ErrorActionPreference = "Stop"

# MES Web 서비스가 먼저 실행되어 있어야 한다.
mvn -s scripts/maven-settings.egov.xml -pl edge-gateway-egov -am package -q | Out-Null
$out = mvn -s scripts/maven-settings.egov.xml -pl edge-gateway-egov exec:java -q 2>&1

if ($out.Contains("[PASS]")) {
    Write-Output "[PASS] gateway smoke"
    exit 0
}

Write-Output "[FAIL] gateway smoke"
exit 1
