# PR-06: DB 컬럼 정의(초안)

## company
- company_id (PK)
- name

## equipment
- device_id (PK)
- company_id (FK)
- name
- status

## telemetry
- telemetry_id (PK)
- company_id (FK)
- device_id (FK)
- timestamp
- metric_key
- metric_value
- unit

## event_log
- event_id (PK)
- company_id (FK)
- device_id (FK)
- event_type
- message
- created_at

## alarm
- alarm_id (PK)
- company_id (FK)
- device_id (FK)
- level
- message
- created_at
