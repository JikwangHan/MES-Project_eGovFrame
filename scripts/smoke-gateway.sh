#!/usr/bin/env sh
set -eu

# 목적: 게이트웨이 시뮬레이터 업링크가 201로 성공하는지 확인한다.
# 이유: PR-03 최소 연동 흐름의 성공 기준을 고정하기 위함이다.
# MES Web 서비스가 먼저 실행되어 있어야 한다.
mvn -s scripts/maven-settings.egov.xml -pl edge-gateway-egov -am package -q >/dev/null
out="$(mvn -s scripts/maven-settings.egov.xml -pl edge-gateway-egov exec:java -q 2>&1)"

printf '%s' "$out" | grep -q "\\[PASS\\]" && { printf '%s\n' "[PASS] gateway smoke"; exit 0; }

printf '%s\n' "[FAIL] gateway smoke"
exit 1
