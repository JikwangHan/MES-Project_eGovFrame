# PR-06: ID 생성 전략(초안)

## 기본 전략
- BIGINT 기반 순차 ID를 사용한다.

## 적용 대상
- telemetry_id, event_id, alarm_id, raw_id

## 비고
- DB별 시퀀스/자동증가 전략은 구현 단계에서 확정한다.
