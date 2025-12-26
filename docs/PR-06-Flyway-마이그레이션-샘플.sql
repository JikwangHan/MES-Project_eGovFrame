-- V1__init.sql (샘플)
create table company (
  company_id varchar(64) not null,
  name varchar(128) not null,
  primary key (company_id)
);

create table equipment (
  device_id varchar(64) not null,
  company_id varchar(64) not null,
  name varchar(128) not null,
  status varchar(32) not null,
  primary key (device_id)
);

create table telemetry (
  telemetry_id bigint not null,
  company_id varchar(64) not null,
  device_id varchar(64) not null,
  raw_id bigint null,
  timestamp timestamp not null,
  metric_key varchar(64) not null,
  metric_value decimal(18,4) not null,
  unit varchar(32) null,
  primary key (telemetry_id)
);
