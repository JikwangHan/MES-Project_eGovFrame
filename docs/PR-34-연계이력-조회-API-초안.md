# PR-34: 외부기관 연계 이력 조회 API 초안

## 목적
- 외부기관 연계 이력 조회를 위한 API 초안을 정의한다.
- 실제 구현 전 계약을 먼저 정리한다.

## 엔드포인트(초안)
- GET /api/external-sync/logs

## 요청 파라미터(예시)
- companyId (선택)
- from (선택, YYYY-MM-DD)
- to (선택, YYYY-MM-DD)
- status (선택, ACCEPTED/FAILED)
- limit (선택, 기본 20)

## 응답(예시)
```json
{
  "result": "OK",
  "message": "",
  "data": [
    {
      "requestId": "SYNC-20250101-0001",
      "companyId": "C-001",
      "from": "2025-01-01",
      "to": "2025-01-31",
      "status": "ACCEPTED",
      "acceptedAt": "2025-01-01T09:00:00",
      "errorCode": "",
      "errorMessage": ""
    }
  ]
}
```

## 비고
- 실제 저장 방식(DB/파일)은 추후 확정
