package com.mes.web.ui;

import java.util.HashMap;
import java.util.Map;

public final class UiRouteDescriptions {
    private static final Map<String, String> DESCRIPTIONS = new HashMap<>();

    static {
        // 공통
        DESCRIPTIONS.put("/ui/login", "로그인 화면: 아이디/비밀번호로 인증한다.");
        DESCRIPTIONS.put("/ui/account/change-password", "암호변경: 로그인 후 비밀번호를 변경한다.");
        // 대시보드
        DESCRIPTIONS.put("/ui/dashboard/production", "생산현황: 시간당 생산량 등 핵심 지표를 요약한다.");
        DESCRIPTIONS.put("/ui/calendar", "일정달력: 작업/납기 일정을 달력으로 관리한다.");
        // 생산관리
        DESCRIPTIONS.put("/ui/orders/summary", "수주현황: 수주 요약 지표를 확인한다.");
        DESCRIPTIONS.put("/ui/orders", "수주내역: 수주/납품/반품 내역을 관리한다.");
        DESCRIPTIONS.put("/ui/work/orders", "작업관리: 작업 현황과 공정/분배를 관리한다.");
        DESCRIPTIONS.put("/ui/work/orders/issue", "작업지시: 작업지시 내역을 관리한다.");
        // 기준정보
        DESCRIPTIONS.put("/ui/master/items", "품목: 품목/부품목록을 관리한다.");
        DESCRIPTIONS.put("/ui/master/item-types", "품목유형: 품목 유형을 분류/관리한다.");
        DESCRIPTIONS.put("/ui/master/processes", "작업공정: 공정 유형과 범주를 관리한다.");
        // 재고관리
        DESCRIPTIONS.put("/ui/inventory/status", "재고현황: 창고별 재고를 조회한다.");
        DESCRIPTIONS.put("/ui/inventory/inbound", "입고내역: 입고 이력을 관리한다.");
        DESCRIPTIONS.put("/ui/inventory/outbound", "출고내역: 출고 이력을 관리한다.");
        DESCRIPTIONS.put("/ui/inventory/requirements", "소요산출: 소요량 계산 결과를 확인한다.");
        // 품질관리
        DESCRIPTIONS.put("/ui/quality/defects/status", "불량현황: 불량 지표를 요약한다.");
        DESCRIPTIONS.put("/ui/quality/defects", "불량내역: 불량 상세 내역을 관리한다.");
        DESCRIPTIONS.put("/ui/quality/defect-types", "불량유형: 불량 유형을 분류/관리한다.");
        // 설비관리
        DESCRIPTIONS.put("/ui/equipment/status", "설비현황: 설비 가동 상태를 확인한다.");
        DESCRIPTIONS.put("/ui/equipment/monitoring", "모니터링: 설비 데이터를 모니터링한다.");
        DESCRIPTIONS.put("/ui/equipment", "설비등록: 설비 정보를 등록/관리한다.");
        // 시스템관리
        DESCRIPTIONS.put("/ui/admin/users", "사용자: 계정 정보를 관리한다.");
        DESCRIPTIONS.put("/ui/admin/permissions", "권한: 역할/권한을 관리한다.");
        DESCRIPTIONS.put("/ui/admin/responsibles", "업무담당자: 담당자 매핑을 관리한다.");
        DESCRIPTIONS.put("/ui/admin/partners", "거래처: 거래처 정보를 관리한다.");
        DESCRIPTIONS.put("/ui/admin/factories-warehouses", "공장/창고: 공장/창고 정보를 관리한다.");
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
