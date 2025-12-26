package com.mes.web.ui;

import java.util.HashMap;
import java.util.Map;

public final class UiRouteDescriptions {
    private static final Map<String, String> DESCRIPTIONS = new HashMap<>();

    static {
        // 공통
        DESCRIPTIONS.put("/ui/login", "로그인 화면: 아이디/비밀번호로 인증한다. 필수 입력: 아이디, 비밀번호. 검증: 공백 불가. 데이터 소스(예정): 사용자/인증 API.");
        DESCRIPTIONS.put("/ui/account/change-password", "암호변경: 로그인 후 비밀번호를 변경한다. 필수 입력: 현재/신규/확인 비밀번호. 검증: 일치 여부. 데이터 소스(예정): 사용자/인증 API.");
        // 대시보드
        DESCRIPTIONS.put("/ui/dashboard/production", "생산현황: 시간당 생산량 등 핵심 지표를 요약한다. 필수 입력: 없음. 검증: 조회 기간. 데이터 소스(예정): 대시보드 집계 API.");
        DESCRIPTIONS.put("/ui/calendar", "일정달력: 작업/납기 일정을 달력으로 관리한다. 필수 입력: 일정명, 날짜. 검증: 기간 유효성. 데이터 소스(예정): 일정/작업 API.");
        // 생산관리
        DESCRIPTIONS.put("/ui/orders/summary", "수주현황: 수주 요약 지표를 확인한다. 필수 입력: 없음. 검증: 조회 조건. 데이터 소스(예정): 수주 집계 API.");
        DESCRIPTIONS.put("/ui/orders", "수주내역: 수주/납품/반품 내역을 관리한다. 필수 입력: 수주번호, 거래처. 검증: 중복 여부. 데이터 소스(예정): 수주/납품/반품 API.");
        DESCRIPTIONS.put("/ui/work/orders", "작업관리: 작업 현황과 공정/분배를 관리한다. 필수 입력: 작업번호, 품목. 검증: 수량 범위. 데이터 소스(예정): 작업/공정 API.");
        DESCRIPTIONS.put("/ui/work/orders/issue", "작업지시: 작업지시 내역을 관리한다. 필수 입력: 지시번호, 일정. 검증: 날짜 범위. 데이터 소스(예정): 작업지시 API.");
        // 기준정보
        DESCRIPTIONS.put("/ui/master/items", "품목: 품목/부품목록을 관리한다. 필수 입력: 품목코드, 품목명. 검증: 코드 중복. 데이터 소스(예정): 품목 API.");
        DESCRIPTIONS.put("/ui/master/item-types", "품목유형: 품목 유형을 분류/관리한다. 필수 입력: 유형명. 검증: 중복 여부. 데이터 소스(예정): 품목유형 API.");
        DESCRIPTIONS.put("/ui/master/processes", "작업공정: 공정 유형과 범주를 관리한다. 필수 입력: 공정명. 검증: 중복 여부. 데이터 소스(예정): 공정 API.");
        // 재고관리
        DESCRIPTIONS.put("/ui/inventory/status", "재고현황: 창고별 재고를 조회한다. 필수 입력: 없음. 검증: 조회 조건. 데이터 소스(예정): 재고 API.");
        DESCRIPTIONS.put("/ui/inventory/inbound", "입고내역: 입고 이력을 관리한다. 필수 입력: 입고일, 품목. 검증: 수량. 데이터 소스(예정): 입고 API.");
        DESCRIPTIONS.put("/ui/inventory/outbound", "출고내역: 출고 이력을 관리한다. 필수 입력: 출고일, 품목. 검증: 재고 수량. 데이터 소스(예정): 출고 API.");
        DESCRIPTIONS.put("/ui/inventory/requirements", "소요산출: 소요량 계산 결과를 확인한다. 필수 입력: 기간. 검증: 기간 유효성. 데이터 소스(예정): 소요산출 API.");
        // 품질관리
        DESCRIPTIONS.put("/ui/quality/defects/status", "불량현황: 불량 지표를 요약한다. 필수 입력: 없음. 검증: 조회 조건. 데이터 소스(예정): 품질 집계 API.");
        DESCRIPTIONS.put("/ui/quality/defects", "불량내역: 불량 상세 내역을 관리한다. 필수 입력: 불량유형, 수량. 검증: 수량 범위. 데이터 소스(예정): 불량 API.");
        DESCRIPTIONS.put("/ui/quality/defect-types", "불량유형: 불량 유형을 분류/관리한다. 필수 입력: 유형명. 검증: 중복 여부. 데이터 소스(예정): 불량유형 API.");
        // 설비관리
        DESCRIPTIONS.put("/ui/equipment/status", "설비현황: 설비 가동 상태를 확인한다. 필수 입력: 없음. 검증: 조회 조건. 데이터 소스(예정): 설비 상태 API.");
        DESCRIPTIONS.put("/ui/equipment/monitoring", "모니터링: 설비 데이터를 모니터링한다. 필수 입력: 설비선택. 검증: 시간 범위. 데이터 소스(예정): 설비 텔레메트리 API.");
        DESCRIPTIONS.put("/ui/equipment", "설비등록: 설비 정보를 등록/관리한다. 필수 입력: 설비코드, 설비명. 검증: 중복 여부. 데이터 소스(예정): 설비 관리 API.");
        // 시스템관리
        DESCRIPTIONS.put("/ui/admin/users", "사용자: 계정 정보를 관리한다. 필수 입력: 사용자ID, 이름. 검증: 중복 여부. 데이터 소스(예정): 사용자 API.");
        DESCRIPTIONS.put("/ui/admin/permissions", "권한: 역할/권한을 관리한다. 필수 입력: 역할명. 검증: 권한 매핑. 데이터 소스(예정): 권한 API.");
        DESCRIPTIONS.put("/ui/admin/responsibles", "업무담당자: 담당자 매핑을 관리한다. 필수 입력: 업무코드, 담당자. 검증: 중복 여부. 데이터 소스(예정): 담당자 API.");
        DESCRIPTIONS.put("/ui/admin/partners", "거래처: 거래처 정보를 관리한다. 필수 입력: 거래처명. 검증: 중복 여부. 데이터 소스(예정): 거래처 API.");
        DESCRIPTIONS.put("/ui/admin/factories-warehouses", "공장/창고: 공장/창고 정보를 관리한다. 필수 입력: 명칭. 검증: 중복 여부. 데이터 소스(예정): 공장/창고 API.");
        DESCRIPTIONS.put("/ui/kpi", "KPI 관리: KPI 정의/목표/산식/단위를 관리한다. 필수 입력: KPI명, 목표값. 검증: 중복 여부. 데이터 소스(예정): KPI API.");
    }

    private UiRouteDescriptions() {
        // 유틸리티 클래스는 인스턴스화하지 않는다.
    }

    public static String describe(String path) {
        if (DESCRIPTIONS.containsKey(path)) {
            return DESCRIPTIONS.get(path);
        }
        return "화면 설명은 추후 문서 기준으로 보강한다.";
    }
}
