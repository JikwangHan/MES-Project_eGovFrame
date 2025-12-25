# PR-04 AI 미들웨어 P0

## 목표
- 원본 데이터 수신 및 보관을 위한 최소 API를 구현한다.
- 표준 적재는 하지 않고 원본 보관만 수행한다.

## 구현 범위
- POST /api/raw-ingest
- 응답: 201 Created
- 데이터: 저장된 원본 ID 반환

## 스모크
- Windows: `scripts/smoke-middleware.ps1`
- Linux/macOS: `scripts/smoke-middleware.sh`

## 검증 기준
- 스모크 실행 결과가 [PASS] 1줄을 출력한다.
