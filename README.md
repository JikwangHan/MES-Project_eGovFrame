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

## 실행 방법 (10줄 이내)
1) 공통 빌드: mvn -s scripts/maven-settings.egov.xml -pl mes-web-egov -am package
2) embedded 실행: mvn -s scripts/maven-settings.egov.xml -pl mes-web-egov -am exec:java
3) 외장 Tomcat 9: mes-web-egov/target/*.war 배포 후 기동
4) 포트: 18080 (다르면 스크립트의 URL만 변경)
5) Windows health: powershell -ExecutionPolicy Bypass -File scripts/smoke-health.ps1
6) Linux/macOS health: sh scripts/smoke-health.sh
7) Windows contracts: powershell -ExecutionPolicy Bypass -File scripts/smoke-contracts.ps1
8) Linux/macOS contracts: sh scripts/smoke-contracts.sh
9) 종료: Ctrl+C

## 문서
- 원칙 정리: docs/00-원칙-정리.md
- PR-00 증빙: docs/PR-00-스캐폴딩.md
- PR-01 증빙: docs/PR-01-계약-정리.md
- 의존성 설정: docs/05-의존성-설정.md
