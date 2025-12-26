# PR-06: DB 컬럼 정의(초안)

## company
- company_id (PK, NOT NULL)
- name (NOT NULL)

## equipment
- device_id (PK, NOT NULL)
- company_id (FK, NOT NULL)
- name (NOT NULL)
- status (NOT NULL)

## telemetry
- telemetry_id (PK, NOT NULL)
- company_id (FK, NOT NULL)
- device_id (FK, NOT NULL)
- raw_id (NULL)
- timestamp (NOT NULL)
- metric_key (NOT NULL)
- metric_value (NOT NULL)
- unit (NULL)
 - 유니크 제약: (company_id, device_id, timestamp, metric_key)

## event_log
- event_id (PK, NOT NULL)
- company_id (FK, NOT NULL)
- device_id (FK, NOT NULL)
- event_type (NOT NULL)
- message (NOT NULL)
- created_at (NOT NULL)

## alarm
- alarm_id (PK, NOT NULL)
- company_id (FK, NOT NULL)
- device_id (FK, NOT NULL)
- level (NOT NULL)
- message (NOT NULL)
- created_at (NOT NULL)

## raw_ingest
- raw_id (PK, NOT NULL)
- company_id (FK, NOT NULL)
- device_id (FK, NULL)
- received_at (NOT NULL)
- payload (NOT NULL)
