package com.mes.web.ui;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UiRouteController {
    // 목적: UI 문서에 정의된 화면 경로를 우선 라우팅만 제공한다.
    // 이유: 데이터 바인딩 전 단계에서 화면 접근 가능 여부를 스모크로 확인하기 위함이다.
    @GetMapping(value = {
        "/ui/login",
        "/ui/account/change-password",
        "/ui/dashboard/production",
        "/ui/calendar",
        "/ui/orders/summary",
        "/ui/orders",
        "/ui/deliveries",
        "/ui/returns",
        "/ui/work/status",
        "/ui/work/orders",
        "/ui/work/orders/issue",
        "/ui/master/items",
        "/ui/master/item-types",
        "/ui/master/processes",
        "/ui/inventory/status",
        "/ui/inventory/inbound",
        "/ui/inventory/outbound",
        "/ui/inventory/requirements",
        "/ui/quality/defects/status",
        "/ui/quality/defects",
        "/ui/quality/defect-types",
        "/ui/equipment/status",
        "/ui/equipment/monitoring",
        "/ui/equipment",
        "/ui/admin/users",
        "/ui/admin/permissions",
        "/ui/admin/responsibles",
        "/ui/admin/partners",
        "/ui/admin/factories-warehouses",
        "/ui/kpi"
    }, produces = MediaType.TEXT_HTML_VALUE)
    public String uiRoute(HttpServletRequest request) {
        String path = request.getRequestURI();
        String description = UiRouteDescriptions.describe(path);
        return UiPageTemplate.render(
            "MES UI",
            "MES UI 스캐폴딩",
            description,
            path
        );
    }
}
