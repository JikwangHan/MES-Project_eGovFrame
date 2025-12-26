# PR-14: ERD(초안)

## 목적
- DB 초안 완료 시점에 ERD를 문서화한다.
- DB 변경 시 동일 문서를 갱신한다.

## ERD(초안)
```mermaid
erDiagram
    COMPANY ||--o{ EQUIPMENT : has
    COMPANY ||--o{ RAW_EVENT : has
    COMPANY ||--o{ NORMALIZED_EVENT : has
    COMPANY ||--o{ KPI : has
    COMPANY ||--o{ ORDERS : has
    COMPANY ||--o{ JOBS : has
    COMPANY ||--o{ ALARM : has
    COMPANY ||--o{ USER_ACCOUNT : has

    EQUIPMENT ||--o{ RAW_EVENT : has
    EQUIPMENT ||--o{ NORMALIZED_EVENT : has
    EQUIPMENT ||--o{ ALARM : has

    RAW_EVENT ||--o{ NORMALIZED_EVENT : has
    KPI ||--o{ KPI_TREND : has
    ORDERS ||--o{ JOBS : has

    COMPANY ||--o{ CODE : has
```

## 비고
- 테이블/관계 변경 시 본 문서를 즉시 갱신한다.
