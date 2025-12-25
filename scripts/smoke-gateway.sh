#!/usr/bin/env sh
set -eu

# MES Web 서비스가 먼저 실행되어 있어야 한다.
mvn -s scripts/maven-settings.egov.xml -pl edge-gateway-egov -am package -q >/dev/null
out="$(mvn -s scripts/maven-settings.egov.xml -pl edge-gateway-egov exec:java -q 2>&1)"

printf '%s' "$out" | grep -q "\\[PASS\\]" && { printf '%s\n' "[PASS] gateway smoke"; exit 0; }

printf '%s\n' "[FAIL] gateway smoke"
exit 1
