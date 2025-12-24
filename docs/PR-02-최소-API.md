# PR-02 최소 API

## 목표
- 계약 문서를 기준으로 최소 API를 구현한다.
- 스모크 스크립트로 200/201 응답을 확인한다.

## 구현 범위
- GET /health
- POST /api/uplink
- POST /api/direct-uplink
- GET /api/equipments
- GET /api/equipments/{deviceId}/telemetry
- GET /api/dashboard/summary

## 스모크
- Windows: `scripts/smoke-api.ps1`
- Linux/macOS: `scripts/smoke-api.sh`

## 검증 기준
- 스모크 실행 결과가 [PASS] 1줄을 출력한다.
