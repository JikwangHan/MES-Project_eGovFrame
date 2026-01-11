# 서비스별 Chat 시작 안내문 (IoT)

이 Chat은 IoT(Edge Gateway) 전용입니다. 아래 문서를 기준으로 작업합니다.

## 1) 공통 기준(필수)
- 공통 기준 정본: docs/User_Request/REQUESTS_MASTER.md (로컬 전용, GitHub 업로드 금지)
- 공통 운영 규칙: docs/operations/CHAT_RULES.md
- PR 운영 방식: docs/operations/PR_WORKFLOW.md

## 2) IoT 전용 통합 가이드(필수)
- docs/operations/SERVICE_CHAT_GUIDE_IOT.md

## 3) 운영 구조/규칙(필수)
- 공통 기준 변경은 공통 기준 Chat에만 요청
- 범위 밖 파일 수정 금지
- 커밋/PR 제목/내용은 한국어로 상세 작성

## 4) 초기 작업 범위(고정)
- edge-gateway-egov/ 내 IoT 관련 소스만 수정
- mes-common/, mes-contracts/, mes-web-egov/, ai-middleware-egov/ 수정 금지

## 5) PR 로드맵/연동 Chart
- 서비스별 PR 로드맵: docs/operations/SERVICE_CHAT_ROADMAP.md
- 연동 Chart: docs/operations/CHAT_HANDOFF_CHARTS.md

## 6) 로그/보안/주석 규칙
- 로그는 [PASS]/[FAIL]/[SKIP]만
- 민감정보 문서/로그 금지
- 주석은 한국어로, 초보자가 이해할 수 있도록 기능/이유 설명

## 7) 범위 밖 변경 처리 절차
1) 범위 밖 변경 발견 시 즉시 작업 중단
2) 변경 목록을 공통 Chat에 요청 형식으로 전달
3) 공통 Chat에서 분리 완료 후 작업 재개
