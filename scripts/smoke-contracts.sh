#!/usr/bin/env sh
set -eu

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
