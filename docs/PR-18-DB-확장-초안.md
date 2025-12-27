# PR-18: DB 스키마/CRUD 확장(초안)

## 목적
- UI 고도화 요구에 맞춰 필요한 데이터 항목을 확장한다.

## 확장 후보(초안)
- Orders: productCode, productName, quantity
- Jobs: equipmentId, operatorName
- KPI: remark, date

## 반영 방식(초안)
1) 현재 단계에서는 API 응답/화면 표시 기준만 확장한다.
2) DB 스키마 확정은 PR-19에서 별도 검토한다.
3) 스키마 확정 시 ERD/DDL을 동시 갱신한다.

## 결정(확정)
- 이번 PR-18에서는 DB 스키마 변경은 하지 않는다.
- 확장 항목은 화면/문서 기준으로만 유지한다.
