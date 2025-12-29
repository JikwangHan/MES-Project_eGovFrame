package com.mes.web.ui;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UiRouteController {
    // -------------------------------------------------------------
    // UI 라우트 컨트롤러
    // 목적: /ui 하위 경로를 공통 템플릿으로 반환한다.
    // 이유: 화면 수가 많아도 라우팅 처리 코드는 단순해야 한다.
    // -------------------------------------------------------------
    // 초보자 설명:
    // - 여러 화면 경로(/ui/...)를 하나의 템플릿으로 처리한다.
    // - 화면이 많아도 "설명 + 공통 레이아웃"은 동일하게 유지된다.
    // 목적: UI 문서에 정의된 화면 경로를 우선 라우팅만 제공한다.
    // 이유: 데이터 바인딩 전 단계에서 화면 접근 가능 여부를 스모크로 확인하기 위함이다.
    // 입력: 브라우저가 요청한 경로.
    // 출력: 공통 레이아웃 HTML(현재는 설명/자리표시자 중심).

    // 목적: HTML 응답에 UTF-8을 명시해 한글 깨짐을 방지한다.
    // 이유: 브라우저 인코딩 자동 판단이 실패할 수 있기 때문이다.
    @GetMapping(value = {
        "/ui/login",
        "/ui/main",
        "/ui/account/change-password",
        "/ui/dashboard/production",
        "/ui/calendar",
        "/ui/orders/summary",
        "/ui/orders",
        "/ui/jobs",
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
        "/ui/kpi",
        "/ui/external-sync/logs"
    }, produces = "text/html; charset=UTF-8")
    public String uiRoute(HttpServletRequest request) {
        // 요청 경로에 맞는 설명을 찾아서 화면에 표시한다.
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
