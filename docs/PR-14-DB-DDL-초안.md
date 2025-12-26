# PR-14: DB DDL 초안(MariaDB)

## 목적
- 스키마 초안을 DDL 형태로 정리한다.

## DDL(초안)
```sql
CREATE TABLE company (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  status VARCHAR(32),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE equipment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  company_id BIGINT NOT NULL,
  device_code VARCHAR(64) NOT NULL,
  name VARCHAR(100) NOT NULL,
  model VARCHAR(100),
  vendor VARCHAR(100),
  status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_equipment_company FOREIGN KEY (company_id) REFERENCES company(id)
);

CREATE TABLE raw_event (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  company_id BIGINT NOT NULL,
  device_id BIGINT,
  payload LONGTEXT NOT NULL,
  received_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_raw_event_device FOREIGN KEY (device_id) REFERENCES equipment(id),
  CONSTRAINT fk_raw_event_company FOREIGN KEY (company_id) REFERENCES company(id)
);

CREATE TABLE normalized_event (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  raw_id BIGINT,
  company_id BIGINT NOT NULL,
  device_id BIGINT,
  protocol_hint VARCHAR(64) NOT NULL DEFAULT 'UNKNOWN',
  event_type VARCHAR(64) NOT NULL DEFAULT 'UNKNOWN',
  event_time DATETIME,
  payload_json LONGTEXT,
  confidence DECIMAL(4,3) NOT NULL DEFAULT 0.000,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_norm_raw FOREIGN KEY (raw_id) REFERENCES raw_event(id),
  CONSTRAINT fk_norm_device FOREIGN KEY (device_id) REFERENCES equipment(id),
  CONSTRAINT fk_norm_company FOREIGN KEY (company_id) REFERENCES company(id)
);

CREATE TABLE kpi (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  company_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  target_value DECIMAL(12,3),
  current_value DECIMAL(12,3),
  unit VARCHAR(32),
  formula VARCHAR(200),
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_kpi_company FOREIGN KEY (company_id) REFERENCES company(id)
);

CREATE TABLE kpi_trend (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  kpi_id BIGINT NOT NULL,
  date DATE NOT NULL,
  target_value DECIMAL(12,3),
  current_value DECIMAL(12,3),
  CONSTRAINT fk_kpi_trend FOREIGN KEY (kpi_id) REFERENCES kpi(id)
);

CREATE TABLE orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  company_id BIGINT NOT NULL,
  order_no VARCHAR(64) NOT NULL,
  partner_name VARCHAR(100),
  due_date DATE,
  status VARCHAR(32) NOT NULL DEFAULT 'PLANNED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_orders_company FOREIGN KEY (company_id) REFERENCES company(id)
);

CREATE TABLE jobs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT,
  company_id BIGINT NOT NULL,
  process_name VARCHAR(100),
  start_time DATETIME,
  end_time DATETIME,
  status VARCHAR(32) NOT NULL DEFAULT 'PLANNED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_jobs_order FOREIGN KEY (order_id) REFERENCES orders(id),
  CONSTRAINT fk_jobs_company FOREIGN KEY (company_id) REFERENCES company(id)
);

CREATE TABLE alarm (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  company_id BIGINT NOT NULL,
  device_id BIGINT,
  level VARCHAR(16) NOT NULL DEFAULT 'WARN',
  message VARCHAR(200),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_alarm_device FOREIGN KEY (device_id) REFERENCES equipment(id),
  CONSTRAINT fk_alarm_company FOREIGN KEY (company_id) REFERENCES company(id)
);

CREATE TABLE user_account (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  company_id BIGINT NOT NULL,
  username VARCHAR(64) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(32) NOT NULL DEFAULT 'USER',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_user_company FOREIGN KEY (company_id) REFERENCES company(id)
);

CREATE TABLE code (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  group_code VARCHAR(64) NOT NULL,
  code VARCHAR(64) NOT NULL,
  name VARCHAR(100) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## 인덱스(초안)
```sql
CREATE INDEX idx_raw_event_company_time ON raw_event(company_id, received_at);
CREATE INDEX idx_raw_event_device_time ON raw_event(device_id, received_at);
CREATE INDEX idx_norm_event_company_time ON normalized_event(company_id, event_time);
CREATE INDEX idx_norm_event_device_time ON normalized_event(device_id, event_time);
CREATE INDEX idx_kpi_trend_kpi_date ON kpi_trend(kpi_id, date);
CREATE INDEX idx_orders_company_no ON orders(company_id, order_no);
CREATE INDEX idx_jobs_company_time ON jobs(company_id, start_time);
```

## 비고
- 컬럼 타입/길이는 추후 확정한다.
