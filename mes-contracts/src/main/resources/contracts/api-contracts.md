# API 계약 (초안)

이 문서는 PR-01 단계의 계약 기준을 고정하기 위한 초안입니다.
코드를 포팅하지 않고 계약과 테스트를 이식한다는 원칙을 따른다.

## 공통 규칙
- 모든 요청/응답은 JSON을 기본으로 한다.
- 모든 응답은 `result`, `message`, `data` 형식을 기본으로 한다.
- 에러는 `errorCode`로 식별한다.

## 1) Health
- GET /health
- 응답: 200 OK
- Body: "OK"

## 2) Uplink Ingest (Gateway -> MES)
- POST /api/uplink
- 요청 필수 필드(예시):
  - companyId
  - deviceId
  - timestamp
  - nonce
  - signature
  - telemetry (표준 정규화 모델)
- 응답: 201 Created

## 3) Direct Ingest (Device -> MES)
- POST /api/direct-uplink
- 제조장비가 MES Web 서비스로 직접 연동될 때 사용한다.
- 프로토콜 정의서에 맞춘 payload만 허용한다.
- 프로토콜 정의서는 추후 확정되며, 다수의 프로토콜을 지원할 수 있다.

## 4) Equipment List
- GET /api/equipments
- 쿼리:
  - status (선택, 유효값만 허용)
  - limit (1..100)
- 응답: 200 OK
- 필드(예시):
  - deviceId
  - lastSeenAt
  - status

## 5) Equipment Telemetry
- GET /api/equipments/{deviceId}/telemetry
- 쿼리:
  - limit (1..100, 기본 20)
- 응답: 200 OK

## 6) Dashboard Summary
- GET /api/dashboard/summary
- 응답: 200 OK
- 필드(예시):
  - okCount
  - warningCount
  - neverCount

## 7) KPI
- GET /api/kpi
- 쿼리:
  - from (기간 시작)
  - to (기간 종료)
  - kpiId (선택)
- 응답: 200 OK
- 필드(예시):
  - kpiId
  - name
  - targetValue
  - currentValue
  - progressRate
  - resultValue
  - unit
  - formula
  - remark
