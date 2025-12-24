# eGovFrame 기반 MES Project (Part 1)

## 목표
- 기존 MES-System(Node 기반)의 서비스 기능/운영 증빙을 100% 유지하면서 eGovFrame으로 재구축
- 코드를 포팅하지 않고 계약(Contract)과 테스트 이식
- JDK 17 + Tomcat 9(embedded) 기준 스캐폴딩

## PR-00 산출물
- mes-web-egov: /health 200
- 공통 로깅 유틸: pass/fail/skip 라인 출력
- smoke 스크립트 1개
- 문서 증빙

## 실행 방법 (10줄 이내)
1) Windows 실행: mvn -pl mes-web-egov spring-boot:run
2) Windows health: powershell -ExecutionPolicy Bypass -File scripts/smoke-health.ps1
3) Linux/macOS 실행: mvn -pl mes-web-egov spring-boot:run
4) Linux/macOS health: sh scripts/smoke-health.sh
5) 종료: Ctrl+C

## 문서
- 원칙 정리: docs/00-원칙-정리.md
- PR-00 증빙: docs/PR-00-스캐폴딩.md
