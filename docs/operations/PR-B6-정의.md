# PR-B6 범위/목표/스모크 기준(미들웨어)

## 1) 작업 목표
- 격리 재처리 승인/거부 흐름을 정의한다.
- 재처리 결과 통계를 최소 단위로 집계한다.

## 2) 작업 범위
- 승인/거부 상태 전환 규칙 정의
- 승인/거부 처리 결과 이력 기록
- 결과 통계(승인/거부 건수) 기본 집계

## 3) 스모크 기준
- 스모크 대상: 승인/거부 처리 흐름 1회 실행
- 스모크 결과 라인:
  - [PASS] middleware quarantine approve/reject smoke ok
- 실행 불가 시:
  - [SKIP] middleware quarantine approve/reject smoke: <사유>

## 4) 완료 기준(코드+스모크+문서)
- 코드: 승인/거부 흐름과 통계 기록 연결 완료
- 스모크: PASS 또는 SKIP 근거 1줄 기록
- 문서: 본 문서 업데이트 + 증빙 라인 기록

## 5) 증빙 기록 위치
- `ai-middleware-egov/docs/PR-B6-스모크-증빙.md`
