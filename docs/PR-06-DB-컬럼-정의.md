# PR-06: DB 컬럼 정의(초안)

## company
- company_id (PK, NOT NULL)
- name (NOT NULL)
- 타입: company_id=VARCHAR, name=VARCHAR
- 길이 기준: company_id=64, name=128

## equipment
- device_id (PK, NOT NULL)
- company_id (FK, NOT NULL)
- name (NOT NULL)
- status (NOT NULL)
- 타입: device_id=VARCHAR, company_id=VARCHAR, name=VARCHAR, status=VARCHAR
- 길이 기준: device_id=64, company_id=64, name=128, status=32
- 삭제 정책: 회사(company) 삭제 시 장비 삭제

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
- 인덱스: company_id, device_id, timestamp
- 인덱스 이름 규칙: idx_telemetry_company_device_time
- 타입: telemetry_id=BIGINT, company_id=VARCHAR, device_id=VARCHAR, raw_id=BIGINT, timestamp=TIMESTAMP, metric_key=VARCHAR, metric_value=DECIMAL, unit=VARCHAR
- 길이 기준: company_id=64, device_id=64, metric_key=64, unit=32, metric_value=DECIMAL(18,4)
- 삭제 정책: 장비(equipment) 삭제 시 텔레메트리 삭제

## event_log
- event_id (PK, NOT NULL)
- company_id (FK, NOT NULL)
- device_id (FK, NOT NULL)
- event_type (NOT NULL)
- message (NOT NULL)
- created_at (NOT NULL)
- 인덱스: company_id, device_id, created_at
- 인덱스 이름 규칙: idx_event_company_device_time
- 타입: event_id=BIGINT, company_id=VARCHAR, device_id=VARCHAR, event_type=VARCHAR, message=VARCHAR, created_at=TIMESTAMP
- 길이 기준: company_id=64, device_id=64, event_type=64, message=255
- 삭제 정책: 장비(equipment) 삭제 시 로그 삭제

## alarm
- alarm_id (PK, NOT NULL)
- company_id (FK, NOT NULL)
- device_id (FK, NOT NULL)
- level (NOT NULL)
- message (NOT NULL)
- created_at (NOT NULL)
- 인덱스: company_id, device_id, created_at
- 인덱스 이름 규칙: idx_alarm_company_device_time
- 타입: alarm_id=BIGINT, company_id=VARCHAR, device_id=VARCHAR, level=VARCHAR, message=VARCHAR, created_at=TIMESTAMP
- 길이 기준: company_id=64, device_id=64, level=32, message=255
- 삭제 정책: 장비(equipment) 삭제 시 알람 삭제

## raw_ingest
- raw_id (PK, NOT NULL)
- company_id (FK, NOT NULL)
- device_id (FK, NULL)
- received_at (NOT NULL)
- payload (NOT NULL)
- 인덱스: company_id, device_id, received_at
- 인덱스 이름 규칙: idx_raw_company_device_time
- 타입: raw_id=BIGINT, company_id=VARCHAR, device_id=VARCHAR, received_at=TIMESTAMP, payload=TEXT
- 길이 기준: company_id=64, device_id=64
- 삭제 정책: 회사(company) 삭제 시 raw_ingest 삭제
