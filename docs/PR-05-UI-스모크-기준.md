# PR-05: UI 스모크 기준(초안)

## 목적
- 화면 스캐폴딩 완료 여부를 PASS/FAIL 라인으로 빠르게 증빙한다.
- 실제 데이터 연결 전 단계에서 렌더링 오류를 조기 탐지한다.

## PASS 기준
- 각 화면 라우팅이 200 응답 또는 렌더링 성공 로그를 남긴다.
- 스모크 스크립트는 [PASS]/[FAIL]/[SKIP] 라인만 출력한다.

## SKIP 기준
- MES 서비스가 기동되지 않았으면 UI 스모크는 SKIP으로 종료한다.
- SKIP 종료 코드는 2로 고정한다.

## 스모크 흐름(예시)
1) health 체크
2) 기본 레이아웃 라우팅 체크(/ui)
3) 화면 목록 라우팅 체크(최소 1개)

## 출력 예시
- [PASS] ui smoke
- [FAIL] ui smoke
- [SKIP] ui smoke

## 스크립트 위치
- Windows: `scripts/smoke-ui.ps1`
- Linux: `scripts/smoke-ui.sh`
