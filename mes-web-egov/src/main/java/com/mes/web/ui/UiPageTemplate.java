package com.mes.web.ui;

public final class UiPageTemplate {
    private UiPageTemplate() {
        // 유틸리티 클래스는 인스턴스화하지 않는다.
    }

    // 목적: UI 스캐폴딩 화면의 공통 레이아웃을 문자열로 제공한다.
    // 이유: 실제 템플릿 엔진 도입 전에도 일관된 레이아웃과 메뉴를 확인하기 위함이다.
    public static String render(String title, String heading, String message, String currentPath) {
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html>");
        html.append("<html lang=\"ko\">");
        html.append("<head>");
        html.append("<meta charset=\"utf-8\">");
        html.append("<title>").append(escape(title)).append("</title>");
        html.append("<style>");
        html.append("body{font-family:Arial, sans-serif;margin:0;background:#f6f7fb;color:#222;}");
        html.append("header{background:#1f2937;color:#fff;padding:12px 20px;}");
        html.append(".wrap{display:flex;min-height:calc(100vh - 48px);}");
        html.append("nav{width:220px;background:#111827;color:#cbd5e1;padding:16px;}");
        html.append("nav a{display:block;color:#cbd5e1;text-decoration:none;padding:6px 0;}");
        html.append("nav a.active{color:#fff;font-weight:bold;}");
        html.append("main{flex:1;padding:20px;}");
        html.append(".card{background:#fff;border-radius:8px;padding:16px;box-shadow:0 1px 3px rgba(0,0,0,0.1);}");
        html.append(".summary{display:flex;gap:12px;margin-bottom:16px;}");
        html.append(".summary .box{flex:1;background:#e5e7eb;border-radius:8px;padding:12px;}");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<header>MES UI 스캐폴딩</header>");
        html.append("<div class=\"wrap\">");
        html.append("<nav>");
        html.append("<div>메뉴</div>");
        for (UiMenu.MenuGroup group : UiMenu.GROUPS) {
            html.append("<div style=\"margin-top:10px;font-weight:bold;\">")
                .append(escape(group.label)).append("</div>");
            for (UiMenu.MenuItem item : group.items) {
                boolean active = item.path.equals(currentPath);
                html.append("<a href=\"").append(item.path).append("\"");
                if (active) {
                    html.append(" class=\"active\"");
                }
                html.append(">").append(escape(item.label)).append("</a>");
            }
        }
        html.append("</nav>");
        html.append("<main>");
        html.append("<div class=\"summary\">");
        html.append("<div class=\"box\">요약 카드 1(데이터 준비 중)</div>");
        html.append("<div class=\"box\">요약 카드 2(데이터 준비 중)</div>");
        html.append("<div class=\"box\">요약 카드 3(데이터 준비 중)</div>");
        html.append("</div>");
        html.append("<div class=\"card\" style=\"margin-bottom:16px;background:#fef3c7;\">");
        html.append("<strong>알림/상태 배너(예정)</strong>");
        html.append("<p>시스템 상태/주의 메시지는 후속 단계에서 적용합니다.</p>");
        html.append("</div>");
        html.append("<div class=\"card\" style=\"margin-bottom:16px;\">");
        html.append("<strong>페이지 설명</strong>");
        html.append("<p>").append(escape(message)).append("</p>");
        html.append("</div>");
        html.append("<div class=\"card\" style=\"margin-bottom:16px;\">");
        html.append("<strong>필터 영역(예정)</strong>");
        html.append("<p>검색 조건/기간 필터는 후속 단계에서 적용합니다.</p>");
        html.append("</div>");
        html.append("<div class=\"card\" style=\"margin-bottom:16px;\">");
        html.append("<strong>테이블 영역(예정)</strong>");
        html.append("<p>그리드/테이블 UI는 후속 단계에서 적용합니다.</p>");
        html.append("</div>");
        html.append("<div class=\"card\" style=\"margin-bottom:16px;\">");
        html.append("<strong>액션 버튼 영역(예정)</strong>");
        html.append("<p>등록/수정/삭제 버튼은 후속 단계에서 적용합니다.</p>");
        html.append("</div>");
        html.append("<div class=\"card\">");
        html.append("<h1>").append(escape(heading)).append("</h1>");
        html.append("<p>요청 경로: ").append(escape(currentPath)).append("</p>");
        html.append("</div>");
        html.append("</main>");
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        return html.toString();
    }

    // 목적: HTML에 직접 출력되는 문자를 최소한으로 이스케이프한다.
    // 이유: 스캐폴딩 단계에서도 기본적인 안전 처리를 보장하기 위함이다.
    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
    }
}
