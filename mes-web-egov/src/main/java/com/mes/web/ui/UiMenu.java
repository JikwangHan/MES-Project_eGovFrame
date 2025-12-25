package com.mes.web.ui;

import java.util.Arrays;
import java.util.List;

public final class UiMenu {
    // 목적: UI 스캐폴딩에서 공통 메뉴 목록을 한 곳에서 관리한다.
    // 이유: 화면 경로가 늘어나도 메뉴 구조를 쉽게 갱신할 수 있도록 하기 위함이다.
    public static final List<MenuItem> ITEMS = Arrays.asList(
        new MenuItem("/ui/login", "로그인"),
        new MenuItem("/ui/dashboard/production", "대시보드"),
        new MenuItem("/ui/orders", "수주관리"),
        new MenuItem("/ui/work/orders", "작업관리"),
        new MenuItem("/ui/master/items", "기준정보"),
        new MenuItem("/ui/inventory/status", "재고관리"),
        new MenuItem("/ui/quality/defects/status", "품질관리"),
        new MenuItem("/ui/equipment/status", "설비관리"),
        new MenuItem("/ui/admin/users", "시스템관리")
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
}
