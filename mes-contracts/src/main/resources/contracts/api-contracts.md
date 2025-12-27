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

## 4-1) Equipment Detail
- GET /api/equipments/{deviceId}
- 응답: 200 OK

## 4-2) Equipment Create
- POST /api/equipments
- 요청 필드(예시):
  - deviceId (선택)
  - name (필수)
  - model (선택)
  - vendor (선택)
  - status (선택)
- 응답: 201 Created

## 4-3) Equipment Update
- PUT /api/equipments/{deviceId}
- 요청 필드(예시):
  - name (선택)
  - model (선택)
  - vendor (선택)
  - status (선택)
- 응답: 200 OK

## 4-4) Equipment Delete
- DELETE /api/equipments/{deviceId}
- 응답: 200 OK

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
  - limit (1..100)
  - offset (0..)
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
  - date
- 응답 예시:
```json
{
  "result": "OK",
  "message": "success",
  "data": [
    {
      "kpiId": "KPI-001",
      "name": "생산성",
      "targetValue": 100,
      "currentValue": 82,
      "progressRate": 0.82,
      "resultValue": 82,
      "unit": "%",
      "formula": "current/target*100",
      "remark": "샘플"
    }
  ]
}
```
- 입력 검증(초안):
  - from/to는 ISO 날짜 형식
  - kpiId는 영문/숫자/하이픈만 허용
- 에러 예시:
```json
{
  "result": "FAIL",
  "message": "invalid request",
  "errorCode": "KPI-400"
}
```

## 7-1) KPI Detail
- GET /api/kpi/{kpiId}
- 응답: 200 OK

## 7-2) KPI Create
- POST /api/kpi
- 요청 필드(예시):
  - name (필수)
  - targetValue (선택)
  - currentValue (선택)
  - unit (선택)
  - formula (선택)
  - remark (선택)
  - date (선택, YYYY-MM-DD)
- 응답: 201 Created

## 7-3) KPI Update
- PUT /api/kpi/{kpiId}
- 요청 필드(예시):
  - name (선택)
  - targetValue (선택)
  - currentValue (선택)
  - unit (선택)
  - formula (선택)
  - remark (선택)
  - date (선택, YYYY-MM-DD)
- 응답: 200 OK

## 7-4) KPI Delete
- DELETE /api/kpi/{kpiId}
- 응답: 200 OK

## 8) KPI Trend
- GET /api/kpi/trend
- 쿼리:
  - from (기간 시작, 선택)
  - to (기간 종료, 선택)
  - kpiId (선택)
  - limit (1..100)
- 응답: 200 OK
- 필드(예시):
  - kpiId
  - name
  - date
  - targetValue
  - currentValue

## 9) Orders
- GET /api/orders
- 쿼리:
  - orderId (선택)
  - partnerName (선택)
  - dueFrom (선택)
  - dueTo (선택)
  - status (선택, PLANNED/IN_PROGRESS/DONE)
  - limit (1..100, 기본 20)
- 응답: 200 OK
- 필드(예시):
  - orderId
  - orderNo
  - productCode
  - productName
  - quantity
  - dueDate
  - status

## 9-1) Order Detail
- GET /api/orders/{orderId}
- 응답: 200 OK

## 9-2) Order Create
- POST /api/orders
- 요청 필드(예시):
  - orderNo (필수)
  - productCode (선택)
  - productName (선택)
  - quantity (선택)
  - partnerName (선택)
  - dueDate (선택, YYYY-MM-DD)
  - status (선택)
- 응답: 201 Created

## 9-3) Order Update
- PUT /api/orders/{orderId}
- 요청 필드(예시):
  - orderNo (선택)
  - productCode (선택)
  - productName (선택)
  - quantity (선택)
  - partnerName (선택)
  - dueDate (선택, YYYY-MM-DD)
  - status (선택)
- 응답: 200 OK

## 9-4) Order Delete
- DELETE /api/orders/{orderId}
- 응답: 200 OK

## 10) Jobs
- GET /api/jobs
- 쿼리:
  - jobId (선택)
  - orderId (선택)
  - processName (선택)
  - from (선택)
  - to (선택)
  - status (선택, PLANNED/IN_PROGRESS/DONE)
  - limit (1..100, 기본 20)
- 응답: 200 OK
- 필드(예시):
  - jobId
  - orderId
  - processName
  - equipmentId
  - operatorName
  - startAt
  - endAt
  - status

## 10-1) Job Detail
- GET /api/jobs/{jobId}
- 응답: 200 OK

## 10-2) Job Create
- POST /api/jobs
- 요청 필드(예시):
  - orderId (필수)
  - processName (필수)
  - startAt (선택)
  - endAt (선택)
  - status (선택)
- 응답: 201 Created

## 10-3) Job Update
- PUT /api/jobs/{jobId}
- 요청 필드(예시):
  - orderId (선택)
  - processName (선택)
  - startAt (선택)
  - endAt (선택)
  - status (선택)
- 응답: 200 OK

## 10-4) Job Delete
- DELETE /api/jobs/{jobId}
- 응답: 200 OK

## 11) External Sync (MES -> External)
- POST /api/external-sync
- 설명: 외부기관 연계를 위한 전송 트리거
- 요청 필드(예시):
  - companyId (선택)
  - from (선택, YYYY-MM-DD)
  - to (선택, YYYY-MM-DD)
- 규칙:
  - from 또는 to 중 하나만 보내는 것은 허용하지 않는다.
  - 둘 다 비어 있으면 전체 기간으로 간주한다.
- 응답: 200 OK
- 응답 필드(예시):
  - requestId
  - status
  - acceptedAt

## 공통 검증 규칙(초안)
- 날짜 형식: YYYY-MM-DD, 월/일 범위 유효성 포함 (형식 오류 시 400)
- 상태 코드: PLANNED/IN_PROGRESS/DONE (형식 오류 시 400)
- 기간 역전(from > to) 금지 (형식 오류 시 400)

## 공통 400 응답 예시
```json
{
  "result": "FAIL",
  "message": "invalid date format",
  "errorCode": "E-1001",
  "data": null
}
```

```json
{
  "result": "FAIL",
  "message": "invalid status",
  "errorCode": "E-1002",
  "data": null
}
```

## 에러코드 요약(초안)
- E-1001: invalid date format
- E-1002: invalid status
- E-1003: invalid date range
- E-1004: invalid numeric value
