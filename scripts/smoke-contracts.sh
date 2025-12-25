#!/usr/bin/env sh
set -eu

# 목적: 계약 문서 파일이 존재하는지 확인한다.
# 이유: 계약 부재 시 구현 기준이 흔들리므로 즉시 실패 처리한다.

files="mes-contracts/src/main/resources/contracts/api-contracts.md \
mes-contracts/src/main/resources/contracts/error-codes.md \
mes-contracts/src/main/resources/contracts/signature-and-normalize.md"

for f in $files; do
  if [ ! -f "$f" ]; then
    printf '%s\n' "[FAIL] contracts missing"
    exit 1
  fi
done

printf '%s\n' "[PASS] contracts ready"
