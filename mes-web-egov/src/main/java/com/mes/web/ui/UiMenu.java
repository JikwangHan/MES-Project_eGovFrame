package com.mes.web.ui;

import java.util.List;

public final class UiMenu {
    // 목적: UI 스캐폴딩에서 공통 메뉴 목록을 한 곳에서 관리한다.
    // 이유: 화면 경로가 늘어나도 메뉴 구조를 쉽게 갱신할 수 있도록 하기 위함이다.
    public static final List<MenuGroup> GROUPS = List.of(
        new MenuGroup("공통", List.of(
            new MenuItem("/ui/login", "로그인"),
            new MenuItem("/ui/account/change-password", "암호변경")
        )),
        new MenuGroup("대시보드", List.of(
            new MenuItem("/ui/dashboard/production", "생산현황"),
            new MenuItem("/ui/calendar", "일정달력")
        )),
        new MenuGroup("생산관리", List.of(
            new MenuItem("/ui/orders/summary", "수주현황"),
            new MenuItem("/ui/orders", "수주내역"),
            new MenuItem("/ui/work/orders", "작업관리"),
            new MenuItem("/ui/work/orders/issue", "작업지시")
        )),
        new MenuGroup("기준정보", List.of(
            new MenuItem("/ui/master/items", "품목"),
            new MenuItem("/ui/master/item-types", "품목유형"),
            new MenuItem("/ui/master/processes", "작업공정")
        )),
        new MenuGroup("재고관리", List.of(
            new MenuItem("/ui/inventory/status", "재고현황"),
            new MenuItem("/ui/inventory/inbound", "입고내역"),
            new MenuItem("/ui/inventory/outbound", "출고내역"),
            new MenuItem("/ui/inventory/requirements", "소요산출")
        )),
        new MenuGroup("품질관리", List.of(
            new MenuItem("/ui/quality/defects/status", "불량현황"),
            new MenuItem("/ui/quality/defects", "불량내역"),
            new MenuItem("/ui/quality/defect-types", "불량유형")
        )),
        new MenuGroup("설비관리", List.of(
            new MenuItem("/ui/equipment/status", "설비현황"),
            new MenuItem("/ui/equipment/monitoring", "모니터링"),
            new MenuItem("/ui/equipment", "설비등록")
        )),
        new MenuGroup("시스템관리", List.of(
            new MenuItem("/ui/admin/users", "사용자"),
            new MenuItem("/ui/admin/permissions", "권한"),
            new MenuItem("/ui/admin/responsibles", "업무담당자"),
            new MenuItem("/ui/admin/partners", "거래처"),
            new MenuItem("/ui/admin/factories-warehouses", "공장/창고")
        ))
    );

    private UiMenu() {
        // 유틸리티 클래스는 인스턴스화하지 않는다.
    }

    public static final class MenuItem {
        public final String path;
        public final String label;

        public MenuItem(String path, String label) {
            this.path = path;
            this.label = label;
        }
    }

    public static final class MenuGroup {
        public final String label;
        public final List<MenuItem> items;

        public MenuGroup(String label, List<MenuItem> items) {
            this.label = label;
            this.items = items;
        }
    }
}
