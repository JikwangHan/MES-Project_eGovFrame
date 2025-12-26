# PR-06: raw_ingest 아카이빙 형식(초안)

## 기본 방식
- 파일 기반 아카이빙(JSON Lines)

## 파일명 규칙
- raw_ingest_YYYYMMDD.jsonl

## 스케줄
- 매일 02:00 배치 아카이빙

## 비고
- 필요 시 DB 아카이빙으로 전환 가능
