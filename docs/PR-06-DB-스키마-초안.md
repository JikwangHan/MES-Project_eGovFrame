# PR-06: DB 스키마 초안

## 목적
- 표준 데이터 모델을 DB 테이블로 정의한다.

## 핵심 테이블(초안)
- company
- equipment
- telemetry
- event_log
- alarm
- raw_ingest

## 비고
- 멀티테넌트 기준은 companyId로 통일한다.
- 개발/실행 단계에서 변경 가능하며, 확정 전까지 수정 가능하다.
- raw_ingest 보관 기간은 30일 기준으로 시작한다.
