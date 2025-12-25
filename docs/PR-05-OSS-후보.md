# PR-05: 오픈소스 모듈 후보(초안)

## 목적
- eGovFrame 적용 가능 여부와 라이선스를 우선 확인한다.
- 상용/제한 라이선스는 제외한다.

## DB
- MariaDB (GPL-2.0) / 운영 표준으로 가장 널리 사용됨

## Grid
- AG Grid Community (MIT) / 기능 풍부, 상용판 제외
- Tabulator (MIT) / 가볍고 문서화가 명확

## Chart
- Apache ECharts (Apache-2.0) / 대시보드 적합
- Chart.js (MIT) / 단순 시각화 적합

## UI/UX Tooling
- Bootstrap (MIT) / 공통 레이아웃/컴포넌트 안정
- AdminLTE (MIT) / 관리자 화면 템플릿

## 비고
- 최종 선택은 UI/UX 설계 문서의 패턴과 맞춤성, 라이선스 검토 결과로 확정한다.

## 1차 선택(초안)
- DB: MariaDB (GPL-2.0)
- Grid: Tabulator (MIT)
- Chart: Apache ECharts (Apache-2.0)
- UI/UX: Bootstrap (MIT)

## 선택 이유(요약)
- 라이선스가 명확하고 무료 사용이 가능하다.
- 문서화가 충분하고 커뮤니티 사용 사례가 많다.
- eGovFrame 기반의 Java/Spring 환경과 함께 사용하기 용이하다.

## 확정 상태
- 현재 단계는 1차 확정이며, 최종 확정은 라이선스 체크리스트 검토 완료 후에 결정한다.
