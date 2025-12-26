# PR-08: API 쿼리 파라미터 초안

## 목적
- 화면 필터 입력과 API 쿼리 파라미터를 1:1로 맞춘다.
- 추후 DB/검색 구현 시 동일한 규칙을 적용한다.

## 공통 규칙(초안)
- 날짜는 `YYYY-MM-DD` 형식을 사용한다.
- limit는 1~100 사이로 제한한다.
- 미입력 시 전체 조회로 처리한다.

## 엔드포인트별 파라미터(초안)
### GET /api/equipments
- status (OK/WARNING/NEVER)
- limit (1..100)

### GET /api/equipments/{deviceId}/telemetry
- limit (1..100)

### GET /api/orders
- orderId (선택, 부분 일치)
- partnerName (선택, 부분 일치)
- dueFrom (선택, YYYY-MM-DD)
- dueTo (선택, YYYY-MM-DD)
- status (PLANNED/IN_PROGRESS/DONE)
- limit (1..100)

### GET /api/jobs
- jobId (선택, 부분 일치)
- orderId (선택, 부분 일치)
- processName (선택, 부분 일치)
- from (선택, YYYY-MM-DD)
- to (선택, YYYY-MM-DD)
- status (PLANNED/IN_PROGRESS/DONE)
- limit (1..100)

### GET /api/kpi
- name (선택, 부분 일치)
- kpiId (선택)
- from (선택, YYYY-MM-DD)
- to (선택, YYYY-MM-DD)
- limit (1..100)

### GET /api/kpi/trend
- kpiId (선택)
- from (선택, YYYY-MM-DD)
- to (선택, YYYY-MM-DD)
- limit (1..100)

## 화면별 필터 → API 매핑(초안)
### /ui/orders
- 수주번호 → orderId
- 거래처명 → partnerName
- 납기일 시작 → dueFrom
- 납기일 종료 → dueTo
- 상태 → status

### /ui/jobs
- 작업 ID → jobId
- 수주번호 → orderId
- 공정명 → processName
- 기간 시작 → from
- 기간 종료 → to
- 상태 → status

### /ui/kpi
- KPI명 → name
- KPI ID → kpiId
- 기간 시작 → from
- 기간 종료 → to
