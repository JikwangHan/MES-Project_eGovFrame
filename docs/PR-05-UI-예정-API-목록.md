# PR-05: UI 예정 API 목록(초안)

## 목적
- 화면별로 예정된 API 호출을 미리 정리한다.
- API 계약(PR-02)과 UI 구현(PR-05)의 연결 기준으로 사용한다.

## 공통
- 로그인: POST /api/auth/login
- 암호변경: POST /api/auth/change-password

## 대시보드
- 생산현황: GET /api/dashboard/production
- 일정달력: GET /api/calendar/events

## 생산관리
- 수주현황: GET /api/orders/summary
- 수주내역: GET /api/orders
- 납품/반품: GET /api/deliveries, GET /api/returns
- 작업관리: GET /api/work/orders
- 작업지시: GET /api/work/orders/issue

## 기준정보
- 품목: GET /api/master/items
- 품목유형: GET /api/master/item-types
- 작업공정: GET /api/master/processes

## 재고관리
- 재고현황: GET /api/inventory/status
- 입고내역: GET /api/inventory/inbound
- 출고내역: GET /api/inventory/outbound
- 소요산출: GET /api/inventory/requirements

## 품질관리
- 불량현황: GET /api/quality/defects/status
- 불량내역: GET /api/quality/defects
- 불량유형: GET /api/quality/defect-types

## 설비관리
- 설비현황: GET /api/equipment/status
- 모니터링: GET /api/equipment/monitoring
- 설비등록: GET /api/equipment

## 시스템관리
- 사용자: GET /api/admin/users
- 권한: GET /api/admin/permissions
- 업무담당자: GET /api/admin/responsibles
- 거래처: GET /api/admin/partners
- 공장/창고: GET /api/admin/factories-warehouses

## 비고
- 실제 계약은 PR-02 문서에 맞춰 확정한다.
- 경로/메서드는 설계 변경 시 갱신한다.
