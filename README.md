# eGovFrame 기반 MES Project (Part 1)

## 목표
- 기존 MES-System(Node 기반)의 서비스 기능/운영 증빙을 100% 유지하면서 eGovFrame으로 재구축
- 코드를 포팅하지 않고 계약(Contract)과 테스트 이식
- JDK 17 + Tomcat 9(외장/embedded) 기준 스캐폴딩

## PR-00 산출물
- mes-web-egov: /health 200
- 공통 로깅 유틸: pass/fail/skip 라인 출력
- smoke 스크립트 1개
- 문서 증빙

## PR-01 산출물
- mes-contracts: API 계약/오류 코드/서명 규칙
- smoke 스크립트 1개

## PR-02 산출물
- mes-web-egov: 최소 API 구현
- smoke 스크립트 1개

## PR-03 산출물
- edge-gateway-egov: 시뮬레이터 기반 uplink
- smoke 스크립트 1개

## PR-04 산출물
- ai-middleware-egov: 원본 수신/보관 P0
- smoke 스크립트 1개

## 실행 방법 (10줄 이내)
1) 공통 빌드: mvn -s scripts/maven-settings.egov.xml -pl mes-web-egov,ai-middleware-egov -am package
2) MES Web 실행: mvn -s scripts/maven-settings.egov.xml -pl mes-web-egov -am exec:java
3) AI 미들웨어 실행: mvn -s scripts/maven-settings.egov.xml -pl ai-middleware-egov exec:java
4) 포트: MES Web 18080, 미들웨어 18081
5) Windows 스모크: health/contracts/api/gateway/middleware 순서로 ps1 실행
6) Linux/macOS 스모크: health/contracts/api/gateway/middleware 순서로 sh 실행
7) 종료: Ctrl+C

## 문서
- 원칙 정리: docs/00-원칙-정리.md
- PR-00 증빙: docs/PR-00-스캐폴딩.md
- PR-01 증빙: docs/PR-01-계약-정리.md
- PR-02 증빙: docs/PR-02-최소-API.md
- PR-03 증빙: docs/PR-03-게이트웨이-설계.md
- PR-04 증빙: docs/PR-04-AI-미들웨어-P0.md
- 의존성 설정: docs/05-의존성-설정.md
- AI 미들웨어 P0 설계: docs/AI-미들웨어-P0-설계.md
