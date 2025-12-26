# PR-14: DB 스키마(초안)

## 목적
- 본개발의 기준이 되는 테이블 구조를 고정한다.

## 설계 원칙(초안)
- 변경 대응: PK는 대체키(자동 증가)로 고정하고, 자연키 변경을 허용한다.
- 다중 관리: company_id, created_by 등을 공통 컬럼으로 두어 기업/사용자별 관리가 쉽도록 한다.
- 중복 최소화: 상태/유형은 코드 테이블로 분리해 SQL을 단순화한다.

## 컬럼 규칙(초안)
- id: BIGINT PK AUTO_INCREMENT
- *_id(FK): BIGINT NOT NULL
- 상태/유형: VARCHAR(32), code 테이블 연계
- 텍스트 본문: LONGTEXT
- 생성 시각: DATETIME DEFAULT CURRENT_TIMESTAMP
- 업데이트 시각: DATETIME DEFAULT CURRENT_TIMESTAMP

## 테이블(초안)
### company
- id (PK)
- name
- status
- created_at

### equipment
- id (PK)
- company_id (FK -> company.id)
- device_code
- name
- model
- vendor
- status
- created_at

### raw_event
- id (PK)
- company_id (FK -> company.id)
- device_id (FK -> equipment.id)
- payload
- received_at

### normalized_event
- id (PK)
- raw_id (FK -> raw_event.id)
- company_id (FK -> company.id)
- device_id (FK -> equipment.id)
- protocol_hint
- event_type
- event_time
- payload_json
- confidence
- created_at

### kpi
- id (PK)
- company_id (FK -> company.id)
- name
- target_value
- current_value
- unit
- formula
- updated_at

### kpi_trend
- id (PK)
- kpi_id (FK -> kpi.id)
- date
- target_value
- current_value

### orders
- id (PK)
- company_id (FK -> company.id)
- order_no
- partner_name
- due_date
- status

### jobs
- id (PK)
- order_id (FK -> orders.id)
- company_id (FK -> company.id)
- process_name
- start_time
- end_time
- status

### alarm
- id (PK)
- company_id (FK -> company.id)
- device_id (FK -> equipment.id)
- level
- message
- created_at

### user_account
- id (PK)
- company_id (FK -> company.id)
- username
- password_hash
- role
- created_at

### code
- id (PK)
- group_code
- code
- name
- sort_order

## 인덱스(초안)
- raw_event(company_id, received_at)
- raw_event(device_id, received_at)
- normalized_event(company_id, event_time)
- normalized_event(device_id, event_time)
- kpi_trend(kpi_id, date)
- orders(company_id, order_no)
- jobs(company_id, start_time)

## 변경 대응 가이드(초안)
- PK는 대체키 유지, 자연키 변경은 컬럼 수정으로 처리
- 컬럼 추가/삭제는 호환성을 위해 단계적으로 진행(기본값/NULL 허용)
- 명칭 변경은 뷰/별칭으로 단계적 전환

## 코드 테이블 연계(초안)
- 상태/유형 컬럼은 code 테이블과 연계한다.
- group_code 예시: EQUIPMENT_STATUS, ORDER_STATUS, JOB_STATUS, ALARM_LEVEL, USER_ROLE

## 비고
- 실제 컬럼 타입/길이는 추후 확정한다.
