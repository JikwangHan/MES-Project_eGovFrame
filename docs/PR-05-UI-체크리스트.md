# PR-05: UI 화면 체크리스트(초안)

## 목적
- UI/UX 문서의 모든 화면을 누락 없이 스캐폴딩하기 위한 목록이다.
- 실제 화면 수/이름은 문서 기준으로 최종 확정한다.

## 사용 방법
1) UI/UX 문서에서 화면 ID, 화면명, 메뉴 경로를 추출한다.
2) 아래 표에 추가하고, 스캐폴딩이 끝나면 상태를 갱신한다.
3) 신규 화면이 생기면 본 표에 먼저 반영한다.

## 화면 목록(작성 중)

| 화면 ID | 화면명 | 메뉴 경로 | 상태 | 비고 |
| --- | --- | --- | --- | --- |
| SCR-0001 | 공통 / 로그인 MES 로그인 화면 | /login | 대기 | 문서 추출 |
| SCR-0002 | 공통 / 로그인 로그인 | /login | 대기 | 문서 추출 |
| SCR-0003 | 공통 / 계정 암호변경 | /account/change-password | 대기 | 문서 추출 |
| SCR-0004 | 대시보드 / 생산현황 현황판 (시간당 생산량) | /dashboard/production | 대기 | 문서 추출 |
| SCR-0005 | 대시보드 / 일정 일정달력 | /calendar | 대기 | 문서 추출 |
| SCR-0006 | 대시보드 / 일정 일정달력 추가 | /calendar | 대기 | 문서 추출 |
| SCR-0007 | 대시보드 / 일정 일정달력 우클릭 메뉴 | /calendar | 대기 | 문서 추출 |
| SCR-0008 | 생산관리 / 수주 수주현황 | /orders/summary | 대기 | 문서 추출 |
| SCR-0009 | 생산관리 / 수주 수주내역과 납품/반품 | /orders | 대기 | 문서 추출 |
| SCR-0010 | 생산관리 / 수주 수주내역 추가 | /orders | 대기 | 문서 추출 |
| SCR-0011 | 생산관리 / 수주 수주내역 변경 | /orders | 대기 | 문서 추출 |
| SCR-0012 | 생산관리 / 납품 납품내역 | /deliveries | 대기 | 문서 추출 |
| SCR-0013 | 생산관리 / 납품 납품내역 추가 | /deliveries | 대기 | 문서 추출 |
| SCR-0014 | 생산관리 / 반품 반품내역 관리 | /returns | 대기 | 문서 추출 |
| SCR-0015 | 생산관리 / 반품 반품내역 추가 | /returns | 대기 | 문서 추출 |
| SCR-0016 | 생산관리 / 작업 작업현황 | /work/status | 대기 | 문서 추출 |
| SCR-0017 | 생산관리 / 작업 작업관리 | /work/orders | 대기 | 문서 추출 |
| SCR-0018 | 생산관리 / 작업 작업관리 추가 | /work/orders | 대기 | 문서 추출 |
| SCR-0019 | 생산관리 / 작업 작업관리 - 생산공정 / 작업분배 | /work/orders | 대기 | 문서 추출 |
| SCR-0020 | 생산관리 / 작업지시 작업지시 | /work/orders/issue | 대기 | 문서 추출 |
| SCR-0021 | 생산관리 / 작업지시 작업지시 추가 | /work/orders/issue | 대기 | 문서 추출 |
| SCR-0022 | 기준정보 / 품목 품목내역과 부품목록 | /master/items | 대기 | 문서 추출 |
| SCR-0023 | 기준정보 / 품목 품목내역 추가 | /master/items | 대기 | 문서 추출 |
| SCR-0024 | 기준정보 / 품목 품목내역과 부품목록 / 생산공정 | /master/items | 대기 | 문서 추출 |
| SCR-0025 | 기준정보 / 품목 품목유형 | /master/item-types | 대기 | 문서 추출 |
| SCR-0026 | 기준정보 / 품목 품목유형 추가 | /master/item-types | 대기 | 문서 추출 |
| SCR-0027 | 기준정보 / 공정 작업공정 | /master/processes | 대기 | 문서 추출 |
| SCR-0028 | 기준정보 / 공정 작업공정 추가 (범주와 공정유형) | /master/processes | 대기 | 문서 추출 |
| SCR-0029 | 재고관리 / 재고 재고현황 | /inventory/status | 대기 | 문서 추출 |
| SCR-0030 | 재고관리 / 입고 재고관리 입고내역 | /inventory/inbound | 대기 | 문서 추출 |
| SCR-0031 | 재고관리 / 입고 입고내역 추가 | /inventory/inbound | 대기 | 문서 추출 |
| SCR-0032 | 재고관리 / 출고 출고내역 | /inventory/outbound | 대기 | 문서 추출 |
| SCR-0033 | 재고관리 / 소요산출 소요산출 | /inventory/requirements | 대기 | 문서 추출 |
| SCR-0034 | 재고관리 / 소요산출 소요산출 결과 | /inventory/requirements | 대기 | 문서 추출 |
| SCR-0035 | 품질관리 / 불량 불량현황 | /quality/defects/status | 대기 | 문서 추출 |
| SCR-0036 | 품질관리 / 불량 불량내역 | /quality/defects | 대기 | 문서 추출 |
| SCR-0037 | 품질관리 / 불량 불량유형 | /quality/defect-types | 대기 | 문서 추출 |
| SCR-0038 | 설비관리 / 모니터링 설비현황 | /equipment/status | 대기 | 문서 추출 |
| SCR-0039 | 설비관리 / 모니터링 모니터링 현황 | /equipment/monitoring | 대기 | 문서 추출 |
| SCR-0040 | 설비관리 / 설비 설비등록 | /equipment | 대기 | 문서 추출 |
| SCR-0041 | 설비관리 / 설비 설비등록 추가 | /equipment | 대기 | 문서 추출 |
| SCR-0042 | 시스템관리 / 사용자 사용자 | /admin/users | 대기 | 문서 추출 |
| SCR-0043 | 시스템관리 / 권한 사용자권한 | /admin/permissions | 대기 | 문서 추출 |
| SCR-0044 | 시스템관리 / 업무담당자 업무 담당자 설정 | /admin/responsibles | 대기 | 문서 추출 |
| SCR-0045 | 시스템관리 / 업무담당자 업무 담당자 | /admin/responsibles | 대기 | 문서 추출 |
| SCR-0046 | 시스템관리 / 거래처 거래처 | /admin/partners | 대기 | 문서 추출 |
| SCR-0047 | 시스템관리 / 거래처 거래처 추가 | /admin/partners | 대기 | 문서 추출 |
| SCR-0048 | 시스템관리 / 공장/창고 생산공장/창고 | /admin/factories-warehouses | 대기 | 문서 추출 |
| SCR-0049 | 시스템관리 / 공장/창고 생산공장/창고 추가 | /admin/factories-warehouses | 대기 | 문서 추출 |
| ADD-KPI-0001 | 시스템관리 / KPI 관리 | /kpi | 대기 | 추가 요구사항 |

## 상태 기준
- 대기: 아직 스캐폴딩하지 않음
- 진행: 화면 골격/라우팅 생성 중
- 완료: 라우팅과 빈 화면 렌더 확인 완료
