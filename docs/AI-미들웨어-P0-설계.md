# AI 미들웨어 P0 설계

## 목표
- 어떤 장비/통신/포맷인지 모르는 원본 데이터를 안전하게 수신하고 보관한다.
- 표준 적재는 하지 않고, 원본 저장과 기본 메타데이터 수집에 집중한다.

## 원본 Envelope 구조(초안)
- receivedAt
- ingressType
- sourceHint (hash, topic, path, port, contentType)
- payload (base64)
- payloadHash (sha256)

## 처리 원칙
- 원본 데이터는 불변 보관한다.
- 표준 테이블 적재는 승인된 계약이 있을 때만 허용한다.
- 신뢰도 미달 또는 검증 실패는 Quarantine으로 격리한다.

## PASS 기준
- `[PASS] ingest.raw stored`
