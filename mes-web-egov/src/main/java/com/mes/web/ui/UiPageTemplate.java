package com.mes.web.ui;

public final class UiPageTemplate {
    private UiPageTemplate() {
        // 유틸리티 클래스는 인스턴스화하지 않는다.
    }

    // 목적: UI 스캐폴딩 화면의 공통 레이아웃을 문자열로 제공한다.
    // 이유: 실제 템플릿 엔진 도입 전에도 일관된 레이아웃과 메뉴를 확인하기 위함이다.
    // 입력: title(브라우저 탭 제목), heading(페이지 제목), message(설명), currentPath(현재 화면 경로).
    // 출력: 완성된 HTML 문자열.
    //
    // 초보자 설명:
    // - 아직 템플릿 엔진(타임리프 등)을 쓰지 않기 때문에,
    //   문자열을 직접 이어붙여 화면을 만든다.
    // - 이 방식은 단순하지만 빠르게 화면 골격을 확인할 수 있다.
    public static String render(String title, String heading, String message, String currentPath) {
        StringBuilder html = new StringBuilder();
        // -------------------------------------------------------------
        // 공통 레이아웃 구성
        // 목적: 모든 화면에서 동일한 헤더/메뉴/기본 스타일을 유지한다.
        // 이유: 화면마다 레이아웃을 반복 구현하면 수정 비용이 커지기 때문이다.
        // -------------------------------------------------------------
        // HTML 문서 시작부를 만든다.
        html.append("<!doctype html>");
        html.append("<html lang=\"ko\">");
        html.append("<head>");
        html.append("<meta charset=\"utf-8\">");
        html.append("<title>").append(escape(title)).append("</title>");
        // 간단한 기본 스타일(색상/레이아웃)을 정의한다.
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
        html.append(".chart{display:flex;align-items:flex-end;gap:6px;height:180px;border:1px solid #e5e7eb;border-radius:6px;padding:8px;}");
        html.append(".chart .bar{width:16px;background:#60a5fa;border-radius:4px 4px 0 0;}");
        html.append(".chart .bar.target{background:#34d399;}");
        html.append(".action-bar{display:flex;gap:8px;flex-wrap:wrap;margin-top:8px;}");
        html.append(".btn{padding:6px 10px;border-radius:6px;border:1px solid #cbd5e1;background:#f8fafc;cursor:pointer;}");
        html.append(".btn.primary{background:#2563eb;color:#fff;border-color:#2563eb;}");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<header>MES UI 스캐폴딩</header>");
        html.append("<div class=\"wrap\">");
        html.append("<nav>");
        html.append("<div>메뉴</div>");
        // -------------------------------------------------------------
        // 메뉴 렌더링
        // 목적: 현재 경로에 따라 활성 메뉴를 강조 표시한다.
        // 이유: 사용자가 현재 위치를 쉽게 인지하도록 돕기 위해서다.
        // -------------------------------------------------------------
        // 메뉴는 UiMenu 정의를 기준으로 반복 출력한다.
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
        // -------------------------------------------------------------
        // 요약 카드 영역
        // 목적: 대시보드 느낌을 주는 상단 요약 정보를 보여준다.
        // 이유: 아직 실제 데이터가 없더라도 화면 구조를 먼저 확인하기 위함이다.
        // -------------------------------------------------------------
        // 요약 카드 영역(대시보드 느낌을 주기 위한 자리표시자).
        html.append("<div class=\"summary\">");
        html.append("<div class=\"box\" id=\"dash-summary-ok\">정상: -</div>");
        html.append("<div class=\"box\" id=\"dash-summary-warning\">경고: -</div>");
        html.append("<div class=\"box\" id=\"dash-summary-never\">미수집: -</div>");
        html.append("</div>");
        // 상단 공지/상태 배너 영역.
        html.append("<div class=\"card\" id=\"status-banner\" style=\"margin-bottom:16px;background:#fef3c7;\">");
        html.append("<strong>알림/상태 배너</strong>");
        html.append("<p id=\"status-banner-msg\">시스템 상태/주의 메시지는 후속 단계에서 적용합니다.</p>");
        html.append("</div>");
        // 현재 화면 설명을 노출한다.
        html.append("<div class=\"card\" style=\"margin-bottom:16px;\">");
        html.append("<strong>페이지 설명</strong>");
        html.append("<p>").append(escape(message)).append("</p>");
        html.append("</div>");
        // 필터(검색) 영역 자리표시자.
        html.append("<div class=\"card\" style=\"margin-bottom:16px;\">");
        html.append("<strong>필터 영역(예정)</strong>");
        html.append("<p>검색 조건/기간 필터는 후속 단계에서 적용합니다.</p>");
        html.append("</div>");
        // 그리드/테이블 영역 자리표시자.
        html.append("<div class=\"card\" style=\"margin-bottom:16px;\">");
        html.append("<strong>테이블 영역(예정)</strong>");
        html.append("<p>그리드/테이블 UI는 후속 단계에서 적용합니다.</p>");
        html.append("</div>");
        // -------------------------------------------------------------
        // 액션 버튼 영역
        // 목적: 등록/수정/삭제/레포팅/외부연계 흐름의 위치를 고정한다.
        // 이유: 실제 기능 구현 전에도 사용자 동선을 확인할 수 있어야 한다.
        // -------------------------------------------------------------
        // 화면 액션 버튼 자리표시자.
        html.append("<div class=\"card\" style=\"margin-bottom:16px;\">");
        html.append("<strong>액션 버튼 영역(예정)</strong>");
        html.append("<div class=\"action-bar\">");
        html.append("<button class=\"btn primary\">등록</button>");
        html.append("<button class=\"btn\">수정</button>");
        html.append("<button class=\"btn\">삭제</button>");
        html.append("<button class=\"btn\" id=\"ui-report\">레포팅</button>");
        html.append("<button class=\"btn\" id=\"ui-external-sync\">외부기관 연계</button>");
        html.append("</div>");
        html.append("<div id=\"report-msg\" style=\"margin-top:8px;color:#0f172a;\"></div>");
        html.append("<div id=\"sync-msg\" style=\"margin-top:6px;color:#0f172a;\"></div>");
        // 외부기관 연계는 스펙이 확정되기 전이라 기본 규칙을 안내한다.
        // 초보자 설명:
        // - from/to 날짜는 함께 제공해야 한다.
        // - 둘 다 비어 있으면 "전체 기간"으로 간주된다.
        html.append("<div style=\"margin-top:6px;color:#475569;font-size:12px;\">");
        html.append("외부기관 연계는 기간(from/to)을 함께 입력해야 합니다. 둘 다 비어 있으면 전체 기간으로 처리됩니다.");
        html.append("</div>");
        // -------------------------------------------------------------
        // 외부기관 연계 이력 표시
        // 목적: 사용자가 최근 요청 결과를 빠르게 확인하도록 로그를 제공한다.
        // 이유: 연계 실패/성공 여부를 즉시 파악할 수 있어야 하기 때문이다.
        // -------------------------------------------------------------
        // 외부기관 연계 요청 이력 표시 영역(간단 로그).
        // 목적: 사용자가 최근 요청 결과를 빠르게 확인하게 한다.
        html.append("<div style=\"margin-top:10px;\">");
        html.append("<strong style=\"font-size:13px;\">연계 요청 이력(최신 5건)</strong>");
        html.append("<ul id=\"sync-history\" style=\"margin-top:6px;padding-left:16px;color:#0f172a;\"></ul>");
        html.append("<table style=\"width:100%;border-collapse:collapse;margin-top:8px;\">");
        html.append("<thead><tr>");
        html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">시간</th>");
        html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">요청ID</th>");
        html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">상태</th>");
        html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">접수시간</th>");
        html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">메시지</th>");
        html.append("</tr></thead>");
        html.append("<tbody id=\"sync-history-body\">");
        html.append("<tr><td colspan=\"5\" style=\"padding:4px 0;\">이력이 없습니다.</td></tr>");
        html.append("</tbody>");
        html.append("</table>");
        html.append("</div>");
        html.append("</div>");
        if ("/ui/external-sync/logs".equals(currentPath)) {
            // 외부기관 연계 이력 조회 화면: 필터 입력과 표 구조를 제공한다.
            // 초보자 설명:
            // - 아직 DB/API가 없더라도 화면에서 입력/검증 흐름을 확인한다.
            html.append("<div class=\"card\" style=\"margin-bottom:16px;\">");
            html.append("<strong>연계 이력 조회(초안)</strong>");
            html.append("<div style=\"display:flex;gap:8px;flex-wrap:wrap;margin-top:8px;\">");
            html.append("<input id=\"sync-from\" placeholder=\"기간 시작(YYYY-MM-DD)\" style=\"padding:6px;\" />");
            html.append("<input id=\"sync-to\" placeholder=\"기간 종료(YYYY-MM-DD)\" style=\"padding:6px;\" />");
            html.append("<input id=\"sync-status\" placeholder=\"상태(ACCEPTED/FAILED)\" style=\"padding:6px;\" />");
            html.append("<button id=\"sync-search\" style=\"padding:6px 10px;\">조회</button>");
            html.append("<button id=\"sync-reset\" style=\"padding:6px 10px;\">초기화</button>");
            html.append("</div>");
            html.append("<div id=\"sync-warning\" style=\"margin-top:8px;color:#b91c1c;\"></div>");
            html.append("</div>");
            html.append("<div class=\"card\" style=\"margin-bottom:16px;\">");
            html.append("<strong>연계 이력 목록(임시)</strong>");
            html.append("<table style=\"width:100%;border-collapse:collapse;margin-top:8px;\">");
            html.append("<thead><tr>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">시간</th>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">요청ID</th>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">상태</th>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">접수시간</th>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">메시지</th>");
            html.append("</tr></thead>");
            html.append("<tbody id=\"sync-log-body\">");
            html.append("<tr><td colspan=\"5\" style=\"padding:4px 0;\">데이터 로딩 중...</td></tr>");
            html.append("</tbody>");
            html.append("</table>");
            html.append("</div>");
        }
        if ("/ui/equipment".equals(currentPath)) {
            // 설비 등록 화면에서 기본 등록 폼을 제공한다.
            // 초보자 설명:
            // - 이 폼은 "등록 버튼"을 눌렀을 때 API를 호출한다.
            // - 입력값이 비어있거나 잘못되면 화면에서 바로 경고한다.
            html.append("<div class=\"card\" style=\"margin-bottom:16px;\">");
            html.append("<strong>설비 등록(초안)</strong>");
            html.append("<div style=\"display:flex;gap:8px;flex-wrap:wrap;margin-top:8px;\">");
            html.append("<input id=\"equip-device\" placeholder=\"장비 ID(선택)\" style=\"padding:6px;\" />");
            html.append("<input id=\"equip-name\" placeholder=\"장비명(필수)\" style=\"padding:6px;\" />");
            html.append("<input id=\"equip-model\" placeholder=\"모델\" style=\"padding:6px;\" />");
            html.append("<input id=\"equip-vendor\" placeholder=\"제조사\" style=\"padding:6px;\" />");
            html.append("<input id=\"equip-status\" placeholder=\"상태(예: ACTIVE)\" style=\"padding:6px;\" />");
            html.append("<button id=\"equip-create\" style=\"padding:6px 10px;\">등록</button>");
            html.append("<button id=\"equip-refresh\" style=\"padding:6px 10px;\">새로고침</button>");
            html.append("</div>");
            html.append("<div id=\"equip-warning\" style=\"margin-top:8px;color:#b91c1c;\"></div>");
            html.append("<div id=\"equip-result\" style=\"margin-top:8px;color:#065f46;\"></div>");
            html.append("</div>");
            html.append("<div class=\"card\" style=\"margin-bottom:16px;\">");
            html.append("<strong>설비 목록(임시)</strong>");
            html.append("<table style=\"width:100%;border-collapse:collapse;margin-top:8px;\">");
            html.append("<thead><tr>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">장비ID</th>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">장비명</th>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">모델</th>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">제조사</th>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">상태</th>");
            html.append("</tr></thead>");
            html.append("<tbody id=\"equip-body\">");
            html.append("<tr><td colspan=\"5\" style=\"padding:4px 0;\">데이터 로딩 중...</td></tr>");
            html.append("</tbody>");
            html.append("</table>");
            html.append("</div>");
        }
        if ("/ui/kpi".equals(currentPath)) {
            // KPI 전용 영역: 요구사항에 맞춘 구성요소(그리드/차트/레포팅) 자리표시자.
            html.append("<div class=\"card\" style=\"margin-bottom:16px;\">");
            html.append("<strong>KPI 그리드 영역(예정)</strong>");
            html.append("<p>KPI명, 목표값, 현재값, 진척률, 결과값, 비고 등을 표시합니다.</p>");
            html.append("</div>");
            // KPI 필터 입력 폼(초안).
            // 이유: 조회 조건을 먼저 화면에 고정해 데이터 바인딩 기준을 맞춘다.
            html.append("<div class=\"card\" style=\"margin-bottom:16px;\">");
            html.append("<strong>KPI 필터(초안)</strong>");
            html.append("<div style=\"display:flex;gap:8px;flex-wrap:wrap;margin-top:8px;\">");
            html.append("<input id=\"kpi-name\" placeholder=\"KPI명\" style=\"padding:6px;\" />");
            html.append("<input id=\"kpi-id\" placeholder=\"KPI ID\" style=\"padding:6px;\" />");
            html.append("<input id=\"kpi-from\" placeholder=\"기간 시작(YYYY-MM-DD)\" style=\"padding:6px;\" />");
            html.append("<input id=\"kpi-to\" placeholder=\"기간 종료(YYYY-MM-DD)\" style=\"padding:6px;\" />");
            html.append("<button id=\"kpi-search\" style=\"padding:6px 10px;\">조회</button>");
            html.append("</div>");
            html.append("<div id=\"kpi-warning\" style=\"margin-top:8px;color:#b91c1c;\"></div>");
            html.append("<p style=\"margin-top:8px;color:#6b7280;\">날짜는 YYYY-MM-DD 형식으로 입력합니다. 비워두면 전체 조회됩니다.</p>");
            html.append("<p style=\"margin-top:4px;color:#6b7280;\">모든 필터가 비어 있으면 전체 조회합니다.</p>");
            html.append("<div id=\"kpi-summary\" style=\"margin-top:6px;color:#6b7280;\"></div>");
            html.append("</div>");
            // 초보자 설명:
            // - KPI 등록은 "KPI명"이 필수이며 숫자 값은 숫자만 허용한다.
            // - 등록 후 목록을 다시 불러와 최신 상태를 보여준다.
            html.append("<div class=\"card\" style=\"margin-bottom:16px;\">");
            html.append("<strong>KPI 등록(초안)</strong>");
            html.append("<div style=\"display:flex;gap:8px;flex-wrap:wrap;margin-top:8px;\">");
            html.append("<input id=\"kpi-create-name\" placeholder=\"KPI명(필수)\" style=\"padding:6px;\" />");
            html.append("<input id=\"kpi-create-target\" placeholder=\"목표값\" style=\"padding:6px;\" />");
            html.append("<input id=\"kpi-create-current\" placeholder=\"현재값\" style=\"padding:6px;\" />");
            html.append("<input id=\"kpi-create-unit\" placeholder=\"단위\" style=\"padding:6px;\" />");
            html.append("<input id=\"kpi-create-formula\" placeholder=\"산식\" style=\"padding:6px;\" />");
            html.append("<button id=\"kpi-create\" style=\"padding:6px 10px;\">등록</button>");
            html.append("</div>");
            html.append("<div id=\"kpi-create-warning\" style=\"margin-top:8px;color:#b91c1c;\"></div>");
            html.append("<div id=\"kpi-create-result\" style=\"margin-top:8px;color:#065f46;\"></div>");
            html.append("</div>");
            html.append("<div class=\"card\" style=\"margin-bottom:16px;\">");
            html.append("<strong>KPI 차트 영역(예정)</strong>");
            html.append("<p>목표값 vs 현재값의 시간별 추이를 표시합니다.</p>");
            html.append("<div id=\"kpi-chart\" class=\"chart\" style=\"margin-top:8px;\"></div>");
            html.append("</div>");
            html.append("<div class=\"card\" style=\"margin-bottom:16px;\">");
            html.append("<strong>KPI 레포팅 영역(예정)</strong>");
            html.append("<p>필터 조건을 반영한 보고서 출력 기능을 제공합니다.</p>");
            html.append("</div>");
            // 샘플 데이터가 화면에 어떻게 보일지 감을 잡기 위한 최소 테이블 예시(그리드).
            html.append("<div class=\"card\" style=\"margin-bottom:16px;\">");
            html.append("<strong>KPI 샘플 데이터(임시)</strong>");
            html.append("<table style=\"width:100%;border-collapse:collapse;margin-top:8px;\">");
            html.append("<thead><tr>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">KPI명</th>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">목표값</th>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">현재값</th>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">진척률</th>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">결과값</th>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">단위</th>");
            html.append("</tr></thead>");
            html.append("<tbody id=\"kpi-body\">");
            html.append("<tr><td colspan=\"6\" style=\"padding:4px 0;\">데이터 로딩 중...</td></tr>");
            html.append("</tbody>");
            html.append("</table>");
            html.append("</div>");
            // KPI 추이(차트 대신 표로 표현) 샘플.
            // 이유: 차트 라이브러리를 붙이기 전에도 "기간별 변화" 영역을 확인할 수 있어야 한다.
            html.append("<div class=\"card\" style=\"margin-bottom:16px;\">");
            html.append("<strong>KPI 추이 샘플(임시)</strong>");
            html.append("<table style=\"width:100%;border-collapse:collapse;margin-top:8px;\">");
            html.append("<thead><tr>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">일자</th>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">목표값</th>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">현재값</th>");
            html.append("</tr></thead>");
            html.append("<tbody id=\"kpi-trend-body\">");
            html.append("<tr><td colspan=\"3\" style=\"padding:4px 0;\">데이터 로딩 중...</td></tr>");
            html.append("</tbody>");
            html.append("</table>");
            html.append("</div>");
        }
        if ("/ui/orders".equals(currentPath)) {
            // 수주 화면에서 리스트가 어떻게 보일지 확인할 수 있도록 샘플 테이블을 추가한다.
            // 필터 입력 폼을 먼저 배치한다.
            html.append("<div class=\"card\" style=\"margin-bottom:16px;\">");
            html.append("<strong>수주 필터(초안)</strong>");
            html.append("<div style=\"display:flex;gap:8px;flex-wrap:wrap;margin-top:8px;\">");
            html.append("<input id=\"order-id\" placeholder=\"수주번호\" style=\"padding:6px;\" />");
            html.append("<input id=\"order-partner\" placeholder=\"거래처명\" style=\"padding:6px;\" />");
            html.append("<input id=\"order-from\" placeholder=\"납기일 시작(YYYY-MM-DD)\" style=\"padding:6px;\" />");
            html.append("<input id=\"order-to\" placeholder=\"납기일 종료(YYYY-MM-DD)\" style=\"padding:6px;\" />");
            html.append("<input id=\"order-status\" placeholder=\"상태(PLANNED/IN_PROGRESS/DONE)\" style=\"padding:6px;\" />");
            html.append("<button id=\"order-search\" style=\"padding:6px 10px;\">조회</button>");
            html.append("</div>");
            html.append("<div id=\"order-warning\" style=\"margin-top:8px;color:#b91c1c;\"></div>");
            html.append("<p style=\"margin-top:8px;color:#6b7280;\">상태는 PLANNED/IN_PROGRESS/DONE 중 하나를 입력합니다.</p>");
            html.append("<p style=\"margin-top:4px;color:#6b7280;\">모든 필터가 비어 있으면 전체 조회합니다.</p>");
            html.append("<div id=\"order-summary\" style=\"margin-top:6px;color:#6b7280;\"></div>");
            html.append("</div>");
            // 초보자 설명:
            // - 아래 "수주 등록" 폼은 API에 POST 요청을 보내 신규 수주를 만든다.
            // - 등록 후 목록을 다시 불러와 최신 상태를 보여준다.
            html.append("<div class=\"card\" style=\"margin-bottom:16px;\">");
            html.append("<strong>수주 등록(초안)</strong>");
            html.append("<div style=\"display:flex;gap:8px;flex-wrap:wrap;margin-top:8px;\">");
            html.append("<input id=\"order-create-no\" placeholder=\"수주번호(필수)\" style=\"padding:6px;\" />");
            html.append("<input id=\"order-create-partner\" placeholder=\"거래처명\" style=\"padding:6px;\" />");
            html.append("<input id=\"order-create-due\" placeholder=\"납기일(YYYY-MM-DD)\" style=\"padding:6px;\" />");
            html.append("<input id=\"order-create-status\" placeholder=\"상태(PLANNED/IN_PROGRESS/DONE)\" style=\"padding:6px;\" />");
            html.append("<button id=\"order-create\" style=\"padding:6px 10px;\">등록</button>");
            html.append("</div>");
            html.append("<div id=\"order-create-warning\" style=\"margin-top:8px;color:#b91c1c;\"></div>");
            html.append("<div id=\"order-create-result\" style=\"margin-top:8px;color:#065f46;\"></div>");
            html.append("</div>");
            html.append("<div class=\"card\" style=\"margin-bottom:16px;\">");
            html.append("<strong>수주 샘플 리스트(임시)</strong>");
            html.append("<table style=\"width:100%;border-collapse:collapse;margin-top:8px;\">");
            html.append("<thead><tr>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">수주번호</th>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">품목코드</th>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">품목명</th>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">수량</th>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">납기일</th>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">상태</th>");
            html.append("</tr></thead>");
            html.append("<tbody id=\"orders-body\">");
            html.append("<tr><td colspan=\"6\" style=\"padding:4px 0;\">데이터 로딩 중...</td></tr>");
            html.append("</tbody>");
            html.append("</table>");
            html.append("</div>");
        }
        if ("/ui/jobs".equals(currentPath)) {
            // 작업 화면에서도 최소 리스트 구성을 확인할 수 있도록 샘플을 보여준다.
            // 필터 입력 폼을 먼저 배치한다.
            html.append("<div class=\"card\" style=\"margin-bottom:16px;\">");
            html.append("<strong>작업 필터(초안)</strong>");
            html.append("<div style=\"display:flex;gap:8px;flex-wrap:wrap;margin-top:8px;\">");
            html.append("<input id=\"job-id\" placeholder=\"작업 ID\" style=\"padding:6px;\" />");
            html.append("<input id=\"job-order\" placeholder=\"수주번호\" style=\"padding:6px;\" />");
            html.append("<input id=\"job-process\" placeholder=\"공정명\" style=\"padding:6px;\" />");
            html.append("<input id=\"job-from\" placeholder=\"기간 시작(YYYY-MM-DD)\" style=\"padding:6px;\" />");
            html.append("<input id=\"job-to\" placeholder=\"기간 종료(YYYY-MM-DD)\" style=\"padding:6px;\" />");
            html.append("<input id=\"job-status\" placeholder=\"상태(PLANNED/IN_PROGRESS/DONE)\" style=\"padding:6px;\" />");
            html.append("<button id=\"job-search\" style=\"padding:6px 10px;\">조회</button>");
            html.append("</div>");
            html.append("<div id=\"job-warning\" style=\"margin-top:8px;color:#b91c1c;\"></div>");
            html.append("<p style=\"margin-top:8px;color:#6b7280;\">기간은 YYYY-MM-DD 형식, 상태는 PLANNED/IN_PROGRESS/DONE 중 하나입니다.</p>");
            html.append("<p style=\"margin-top:4px;color:#6b7280;\">모든 필터가 비어 있으면 전체 조회합니다.</p>");
            html.append("<div id=\"job-summary\" style=\"margin-top:6px;color:#6b7280;\"></div>");
            html.append("</div>");
            // 초보자 설명:
            // - "작업 등록"은 수주번호와 공정명을 필수로 받는다.
            // - 입력값이 잘못되면 서버 호출 전에 바로 안내한다.
            html.append("<div class=\"card\" style=\"margin-bottom:16px;\">");
            html.append("<strong>작업 등록(초안)</strong>");
            html.append("<div style=\"display:flex;gap:8px;flex-wrap:wrap;margin-top:8px;\">");
            html.append("<input id=\"job-create-order\" placeholder=\"수주번호(필수)\" style=\"padding:6px;\" />");
            html.append("<input id=\"job-create-process\" placeholder=\"공정명(필수)\" style=\"padding:6px;\" />");
            html.append("<input id=\"job-create-start\" placeholder=\"시작(YYYY-MM-DDTHH:MM:SS)\" style=\"padding:6px;\" />");
            html.append("<input id=\"job-create-end\" placeholder=\"종료(YYYY-MM-DDTHH:MM:SS)\" style=\"padding:6px;\" />");
            html.append("<input id=\"job-create-status\" placeholder=\"상태(PLANNED/IN_PROGRESS/DONE)\" style=\"padding:6px;\" />");
            html.append("<button id=\"job-create\" style=\"padding:6px 10px;\">등록</button>");
            html.append("</div>");
            html.append("<div id=\"job-create-warning\" style=\"margin-top:8px;color:#b91c1c;\"></div>");
            html.append("<div id=\"job-create-result\" style=\"margin-top:8px;color:#065f46;\"></div>");
            html.append("</div>");
            html.append("<div class=\"card\" style=\"margin-bottom:16px;\">");
            html.append("<strong>작업 샘플 리스트(임시)</strong>");
            html.append("<table style=\"width:100%;border-collapse:collapse;margin-top:8px;\">");
            html.append("<thead><tr>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">작업ID</th>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">수주번호</th>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">공정명</th>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">시작</th>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">종료</th>");
            html.append("<th style=\"border-bottom:1px solid #ddd;text-align:left;\">상태</th>");
            html.append("</tr></thead>");
            html.append("<tbody id=\"jobs-body\">");
            html.append("<tr><td colspan=\"6\" style=\"padding:4px 0;\">데이터 로딩 중...</td></tr>");
            html.append("</tbody>");
            html.append("</table>");
            html.append("</div>");
        }
        if ("/ui/kpi".equals(currentPath) || "/ui/orders".equals(currentPath)
            || "/ui/jobs".equals(currentPath) || "/ui/equipment".equals(currentPath)
            || "/ui/dashboard/production".equals(currentPath)) {
            // -------------------------------------------------------------
            // 공통 스크립트 영역
            // 목적: 테이블 채우기/검증/메시지 처리 등 공통 JS 유틸을 제공한다.
            // 이유: 화면마다 중복된 스크립트를 줄이고 유지보수를 쉽게 한다.
            // -------------------------------------------------------------
            // 목적: 화면에서 API 샘플 데이터를 가져와 표 형태로 표시한다.
            // 이유: 실제 DB/차트 연동 전에 데이터 흐름을 검증하기 위함이다.
            // 초보자 설명:
            // - 아래 스크립트는 "조회 버튼 클릭 → API 호출 → 표 갱신" 흐름을 수행한다.
            // - 아직 복잡한 프레임워크를 쓰지 않고, 가장 단순한 방식으로 동작시킨다.
            html.append("<script>");
            html.append("(function(){");
            html.append("function fillTable(bodyId, rows, columns){");
            html.append("var body=document.getElementById(bodyId);");
            html.append("if(!body){return;}");
            html.append("body.innerHTML=\"\";");
            html.append("if(!rows||rows.length===0){");
            html.append("var tr=document.createElement(\"tr\");");
            html.append("var td=document.createElement(\"td\");");
            html.append("td.colSpan=columns.length;");
            html.append("td.textContent=\"데이터 없음\";");
            html.append("tr.appendChild(td);");
            html.append("body.appendChild(tr);");
            html.append("return;}");
            html.append("rows.forEach(function(row){");
            html.append("var tr=document.createElement(\"tr\");");
            html.append("columns.forEach(function(col){");
            html.append("var td=document.createElement(\"td\");");
            html.append("var v=row&&row[col]!==undefined?row[col]:\"\";");
            html.append("td.textContent=v;");
            html.append("tr.appendChild(td);");
            html.append("});");
            html.append("body.appendChild(tr);");
            html.append("});");
            html.append("}");
            html.append("function renderKpiChart(rows){");
            html.append("var container=document.getElementById(\"kpi-chart\");");
            html.append("if(!container){return;}");
            html.append("container.innerHTML=\"\";");
            html.append("if(!rows||rows.length===0){");
            html.append("container.textContent=\"데이터 없음\";");
            html.append("return;}");
            html.append("var max=0;");
            html.append("rows.forEach(function(row){");
            html.append("max=Math.max(max, Number(row.targetValue||0), Number(row.currentValue||0));");
            html.append("});");
            html.append("max=max||1;");
            html.append("rows.forEach(function(row){");
            html.append("var target=document.createElement(\"div\");");
            html.append("target.className=\"bar target\";");
            html.append("target.style.height=((Number(row.targetValue||0)/max)*100)+\"%\";");
            html.append("target.title=\"목표값 \" + (row.targetValue||0);");
            html.append("var current=document.createElement(\"div\");");
            html.append("current.className=\"bar\";");
            html.append("current.style.height=((Number(row.currentValue||0)/max)*100)+\"%\";");
            html.append("current.title=\"현재값 \" + (row.currentValue||0);");
            html.append("container.appendChild(target);");
            html.append("container.appendChild(current);");
            html.append("});");
            html.append("}");
            html.append("function fetchJson(url, cb, onError){");
            html.append("fetch(url).then(function(res){");
            html.append("if(!res.ok){return res.json().then(function(data){throw data;});}");
            html.append("return res.json();");
            html.append("}).then(cb).catch(function(err){");
            html.append("if(onError){onError(err);}");
            html.append("});");
            html.append("}");
            html.append("function postJson(url, payload, cb, onError){");
            html.append("fetch(url,{method:\"POST\",headers:{\"Content-Type\":\"application/json\"},body:JSON.stringify(payload||{})})");
            html.append(".then(function(res){");
            html.append("if(!res.ok){return res.json().then(function(data){throw data;});}");
            html.append("return res.json();");
            html.append("}).then(cb).catch(function(err){");
            html.append("if(onError){onError(err);}");
            html.append("});");
            html.append("}");
            html.append("function buildQuery(params){");
            html.append("var query=[];");
            html.append("Object.keys(params).forEach(function(key){");
            html.append("var v=params[key];");
            html.append("if(v){query.push(encodeURIComponent(key)+\"=\"+encodeURIComponent(v));}");
            html.append("});");
            html.append("return query.length?\"?\"+query.join(\"&\"): \"\";");
            html.append("}");
            html.append("function resolveMessage(err){");
            html.append("if(!err){return \"요청 오류\";}");
            html.append("var code=err.errorCode||\"\";");
            html.append("if(code===\"E-0001\"){return \"필수 입력값이 누락되었습니다. 입력값을 확인하세요.\";}");
            html.append("if(code===\"E-1001\"){return \"날짜 형식 또는 범위가 올바르지 않습니다.\";}");
            html.append("if(code===\"E-1002\"){return \"상태 값이 올바르지 않습니다.\";}");
            html.append("if(code===\"E-1003\"){return \"기간 시작이 종료보다 늦을 수 없습니다.\";}");
            html.append("if(code===\"E-1004\"){return \"숫자 형식이 올바르지 않습니다.\";}");
            html.append("if(code===\"E-404\"){return \"대상을 찾을 수 없습니다. 다시 확인해 주세요.\";}");
            html.append("return err.message||\"요청 오류\";");
            html.append("}");
            html.append("function setWarning(id, err){");
            html.append("var el=document.getElementById(id);");
            html.append("if(!el){return;}");
            html.append("var code=err&&err.errorCode?\"[\"+err.errorCode+\"] \":\"\";");
            html.append("el.textContent=code+resolveMessage(err);");
            html.append("}");
            html.append("function setDisabled(id, disabled){");
            html.append("var btn=document.getElementById(id);");
            html.append("if(!btn){return;}");
            html.append("btn.disabled=disabled;");
            html.append("btn.style.opacity=disabled?\"0.6\":\"1\";");
            html.append("}");
            html.append("function labelFor(key){");
            html.append("var map={");
            html.append("orderId:\"수주번호\",partnerName:\"거래처명\",dueFrom:\"납기일 시작\",dueTo:\"납기일 종료\",status:\"상태\",");
            html.append("jobId:\"작업 ID\",processName:\"공정명\",from:\"기간 시작\",to:\"기간 종료\",");
            html.append("name:\"KPI명\",kpiId:\"KPI ID\"");
            html.append("};");
            html.append("return map[key]||key;");
            html.append("}");
            html.append("function setSummary(id, params){");
            html.append("var el=document.getElementById(id);");
            html.append("if(!el){return;}");
            html.append("function normalize(value){");
            html.append("if(value===null||value===undefined){return \"\";}");
            html.append("var text=String(value);");
            html.append("text=text.replace(/\\s+/g,\" \").trim();");
            html.append("text=text.replace(/[<>\\\"]/g,\"\");");
            html.append("return text;");
            html.append("}");
            html.append("function shrink(value){");
            html.append("var text=normalize(value);");
            html.append("if(!text){return \"\";}");
            html.append("return text.length>12?text.substring(0,12)+\"...\":text;");
            html.append("}");
            html.append("var cleaned={};");
            html.append("Object.keys(params).forEach(function(k){");
            html.append("var v=normalize(params[k]);");
            html.append("if(v){cleaned[k]=v;}");
            html.append("});");
            html.append("var keys=Object.keys(cleaned);");
            html.append("var order=[\"orderId\",\"partnerName\",\"dueFrom\",\"dueTo\",\"status\",\"jobId\",\"processName\",\"from\",\"to\",\"name\",\"kpiId\"];");
            html.append("keys.sort(function(a,b){");
            html.append("var ia=order.indexOf(a);");
            html.append("var ib=order.indexOf(b);");
            html.append("if(ia===-1&&ib===-1){return a.localeCompare(b);}"); 
            html.append("if(ia===-1){return 1;}");
            html.append("if(ib===-1){return -1;}");
            html.append("return ia-ib;");
            html.append("});");
            html.append("if(keys.length===0){");
            html.append("el.textContent=\"최근 조회 조건: 전체\";");
            html.append("return;");
            html.append("}");
            html.append("var limited=keys.slice(0,4);");
            html.append("var parts=limited.map(function(k){return labelFor(k)+\"=\"+shrink(cleaned[k]);});");
            html.append("var suffix=keys.length>4?\" 외 \"+(keys.length-4)+\"건\":\"\";");
            html.append("el.textContent=\"최근 조회 조건: \"+parts.join(\", \")+suffix;");
            html.append("}");
            html.append("function setInvalid(ids, invalid){");
            html.append("ids.forEach(function(id){");
            html.append("var el=document.getElementById(id);");
            html.append("if(!el){return;}");
            html.append("el.style.border=invalid?\"1px solid #dc2626\":\"1px solid #d1d5db\";");
            html.append("el.style.background=invalid?\"#fef2f2\":\"#fff\";");
            html.append("});");
            html.append("}");
            html.append("function isValidDate(value){");
            html.append("if(!value){return true;}");
            html.append("if(!/^\\d{4}-\\d{2}-\\d{2}$/.test(value)){return false;}");
            html.append("var parts=value.split(\"-\");");
            html.append("var y=parseInt(parts[0],10);");
            html.append("var m=parseInt(parts[1],10);");
            html.append("var d=parseInt(parts[2],10);");
            html.append("if(m<1||m>12){return false;}");
            html.append("var days=[31,28,31,30,31,30,31,31,30,31,30,31];");
            html.append("var leap=(y%4===0&&y%100!==0)||(y%400===0);");
            html.append("if(leap){days[1]=29;}");
            html.append("if(d<1||d>days[m-1]){return false;}");
            html.append("return true;");
            html.append("}");
            html.append("function isValidDateRange(from,to){");
            html.append("if(!from||!to){return true;}");
            html.append("return from<=to;");
            html.append("}");
            html.append("function isValidStatus(value){");
            html.append("if(!value){return true;}");
            html.append("var upper=value.toUpperCase();");
            html.append("return upper===\"PLANNED\"||upper===\"IN_PROGRESS\"||upper===\"DONE\";");
            html.append("}");
            html.append("function isValidSyncStatus(value){");
            html.append("if(!value){return true;}");
            html.append("var upper=value.toUpperCase();");
            html.append("return upper===\"ACCEPTED\"||upper===\"FAILED\";");
            html.append("}");
            html.append("function validateOrder(){");
            html.append("var from=document.getElementById(\"order-from\").value;");
            html.append("var to=document.getElementById(\"order-to\").value;");
            html.append("var status=document.getElementById(\"order-status\").value;");
            html.append("if(!isValidDate(from)||!isValidDate(to)){");
            html.append("return \"납기일 형식이 올바르지 않습니다. YYYY-MM-DD로 입력하세요.\";");
            html.append("}");
            html.append("if(!isValidDateRange(from,to)){");
            html.append("return \"납기일 시작이 종료보다 늦을 수 없습니다.\";");
            html.append("}");
            html.append("if(!isValidStatus(status)){");
            html.append("return \"수주 상태 값이 올바르지 않습니다.\";");
            html.append("}");
            html.append("return \"\";");
            html.append("}");
            html.append("function validateJob(){");
            html.append("var from=document.getElementById(\"job-from\").value;");
            html.append("var to=document.getElementById(\"job-to\").value;");
            html.append("var status=document.getElementById(\"job-status\").value;");
            html.append("if(!isValidDate(from)||!isValidDate(to)){");
            html.append("return \"작업 기간 형식이 올바르지 않습니다. YYYY-MM-DD로 입력하세요.\";");
            html.append("}");
            html.append("if(!isValidDateRange(from,to)){");
            html.append("return \"작업 기간 시작이 종료보다 늦을 수 없습니다.\";");
            html.append("}");
            html.append("if(!isValidStatus(status)){");
            html.append("return \"작업 상태 값이 올바르지 않습니다.\";");
            html.append("}");
            html.append("return \"\";");
            html.append("}");
            html.append("function validateKpi(){");
            html.append("var from=document.getElementById(\"kpi-from\").value;");
            html.append("var to=document.getElementById(\"kpi-to\").value;");
            html.append("if(!isValidDate(from)||!isValidDate(to)){");
            html.append("return \"KPI 기간 형식이 올바르지 않습니다. YYYY-MM-DD로 입력하세요.\";");
            html.append("}");
            html.append("if(!isValidDateRange(from,to)){");
            html.append("return \"KPI 기간 시작이 종료보다 늦을 수 없습니다.\";");
            html.append("}");
            html.append("return \"\";");
            html.append("}");
            html.append("function setSyncMessage(parts){");
            html.append("var el=document.getElementById(\"sync-msg\");");
            html.append("if(!el){return;}");
            html.append("if(parts&&parts.length>0){el.textContent=parts.join(\" | \");return;}");
            html.append("el.textContent=\"연계 요청 완료\";");
            html.append("}");
            html.append("function setBannerMessage(text){");
            html.append("var banner=document.getElementById(\"status-banner-msg\");");
            html.append("if(banner){banner.textContent=text;}");
            html.append("}");
            // 대시보드 요약 카드 값을 안전하게 채운다.
            // 이유: 응답이 비어 있어도 화면이 깨지지 않도록 하기 위함이다.
            html.append("function setDashText(id,label,value){");
            html.append("var el=document.getElementById(id);");
            html.append("if(!el){return;}");
            html.append("var safe=(value===null||value===undefined)?\"-\":value;");
            html.append("el.textContent=label+\": \"+safe;");
            html.append("}");
            html.append("function nowStamp(){");
            html.append("return (new Date()).toLocaleString();");
            html.append("}");
            // 대시보드 요약 조회
            // 목적: 홈(메인) 화면 요약 카드를 API 데이터로 채운다.
            // 이유: 집계 로직 준비 전에도 화면 흐름을 검증해야 한다.
            html.append("if(document.getElementById(\"dash-summary-ok\")){");
            html.append("fetchJson(\"/api/dashboard/summary\",function(res){");
            html.append("var data=(res&&res.data)||{};");
            html.append("setDashText(\"dash-summary-ok\",\"정상\",data.okCount);");
            html.append("setDashText(\"dash-summary-warning\",\"경고\",data.warningCount);");
            html.append("setDashText(\"dash-summary-never\",\"미수집\",data.neverCount);");
            html.append("var warn=Number(data.warningCount||0);");
            html.append("var never=Number(data.neverCount||0);");
            html.append("if(warn>0||never>0){");
            html.append("setBannerMessage(\"주의: 경고/미수집 상태가 있습니다.\");");
            html.append("}else{");
            html.append("setBannerMessage(\"시스템 상태 정상(임시)\");");
            html.append("}");
            html.append("},function(err){");
            html.append("setBannerMessage(\"대시보드 요약 조회 실패\");");
            html.append("});");
            html.append("}");
            html.append("var syncHistory=[];");
            html.append("var syncHistoryRows=[];");
            html.append("function addSyncHistory(text){");
            html.append("if(!text){return;}");
            html.append("syncHistory.unshift(text);");
            html.append("if(syncHistory.length>5){syncHistory=syncHistory.slice(0,5);}");
            html.append("var list=document.getElementById(\"sync-history\");");
            html.append("if(!list){return;}");
            html.append("list.innerHTML=\"\";");
            html.append("for(var i=0;i<syncHistory.length;i++){");
            html.append("var li=document.createElement(\"li\");");
            html.append("li.textContent=syncHistory[i];");
            html.append("list.appendChild(li);");
            html.append("}");
            html.append("}");
            html.append("function addSyncHistoryRow(row){");
            html.append("if(!row){return;}");
            html.append("syncHistoryRows.unshift(row);");
            html.append("if(syncHistoryRows.length>5){syncHistoryRows=syncHistoryRows.slice(0,5);}");
            html.append("var body=document.getElementById(\"sync-history-body\");");
            html.append("if(!body){return;}");
            html.append("body.innerHTML=\"\";");
            html.append("for(var i=0;i<syncHistoryRows.length;i++){");
            html.append("var r=syncHistoryRows[i];");
            html.append("var tr=document.createElement(\"tr\");");
            html.append("function td(text){");
            html.append("var cell=document.createElement(\"td\");");
            html.append("cell.style.padding=\"4px 0\";");
            html.append("cell.textContent=text||\"\";");
            html.append("return cell;");
            html.append("}");
            html.append("tr.appendChild(td(r.time));");
            html.append("tr.appendChild(td(r.requestId));");
            html.append("tr.appendChild(td(r.status));");
            html.append("tr.appendChild(td(r.acceptedAt));");
            html.append("tr.appendChild(td(r.message));");
            html.append("body.appendChild(tr);");
            html.append("}");
            html.append("}");
            html.append("var reportBtn=document.getElementById(\"ui-report\");");
            html.append("if(reportBtn){");
            html.append("reportBtn.addEventListener(\"click\",function(){");
            html.append("var el=document.getElementById(\"report-msg\");");
            html.append("if(el){el.textContent=\"레포팅 기능은 후속 단계에서 파일 다운로드로 연결됩니다.\";}");
            html.append("});");
            html.append("}");
            // -------------------------------------------------------------
            // 외부기관 연계 버튼 처리
            // 목적: 사용자가 클릭했을 때 기간 검증 후 연계 API를 호출한다.
            // 이유: 서버로 요청을 보내기 전에 기본 오류를 차단해야 하기 때문이다.
            // -------------------------------------------------------------
            html.append("var syncBtn=document.getElementById(\"ui-external-sync\");");
            html.append("if(syncBtn){");
            html.append("syncBtn.addEventListener(\"click\",function(){");
            html.append("var fromEl=document.getElementById(\"order-from\")||document.getElementById(\"kpi-from\");");
            html.append("var toEl=document.getElementById(\"order-to\")||document.getElementById(\"kpi-to\");");
            html.append("var payload={from:fromEl?fromEl.value:\"\",to:toEl?toEl.value:\"\"};");
            html.append("var fromVal=payload.from||\"\";");
            html.append("var toVal=payload.to||\"\";");
            html.append("if((fromVal&&!toVal)||(!fromVal&&toVal)){");
            html.append("setSyncMessage([\"기간 시작/종료는 함께 입력해야 합니다.\"]);");
            html.append("setBannerMessage(\"외부기관 연계 요청에 실패했습니다.\");");
            html.append("addSyncHistory(nowStamp()+\" - 실패: 기간 입력 누락\");");
            html.append("addSyncHistoryRow({");
            html.append("time:nowStamp(),");
            html.append("requestId:\"\",");
            html.append("status:\"FAILED\",");
            html.append("acceptedAt:\"\",");
            html.append("message:\"기간 시작/종료는 함께 입력해야 합니다.\"");
            html.append("});");
            html.append("return;");
            html.append("}");
            html.append("if(!isValidDate(fromVal)||!isValidDate(toVal)){");
            html.append("setSyncMessage([\"날짜 형식이 올바르지 않습니다. YYYY-MM-DD로 입력하세요.\"]);");
            html.append("setBannerMessage(\"외부기관 연계 요청에 실패했습니다.\");");
            html.append("addSyncHistory(nowStamp()+\" - 실패: 날짜 형식 오류\");");
            html.append("addSyncHistoryRow({");
            html.append("time:nowStamp(),");
            html.append("requestId:\"\",");
            html.append("status:\"FAILED\",");
            html.append("acceptedAt:\"\",");
            html.append("message:\"날짜 형식이 올바르지 않습니다.\"");
            html.append("});");
            html.append("return;");
            html.append("}");
            html.append("if(!isValidDateRange(fromVal,toVal)){");
            html.append("setSyncMessage([\"기간 시작이 종료보다 늦을 수 없습니다.\"]);");
            html.append("setBannerMessage(\"외부기관 연계 요청에 실패했습니다.\");");
            html.append("addSyncHistory(nowStamp()+\" - 실패: 기간 역전\");");
            html.append("addSyncHistoryRow({");
            html.append("time:nowStamp(),");
            html.append("requestId:\"\",");
            html.append("status:\"FAILED\",");
            html.append("acceptedAt:\"\",");
            html.append("message:\"기간 시작이 종료보다 늦을 수 없습니다.\"");
            html.append("});");
            html.append("return;");
            html.append("}");
            html.append("postJson(\"/api/external-sync\",payload,function(res){");
            html.append("var data=res&&res.data?res.data:{};");
            html.append("var parts=[];");
            html.append("if(data.requestId){parts.push(\"요청ID: \"+data.requestId);}");
            html.append("if(data.status){parts.push(\"상태: \"+data.status);}");
            html.append("if(data.acceptedAt){parts.push(\"접수시간: \"+data.acceptedAt);}");
            html.append("setSyncMessage(parts);");
            html.append("setBannerMessage(\"외부기관 연계 요청이 접수되었습니다.\");");
            html.append("var now=nowStamp();");
            html.append("var summary=parts.length>0?parts.join(\" | \"):\"연계 요청 완료\";");
            html.append("addSyncHistory(now+\" - \"+summary);");
            html.append("addSyncHistoryRow({");
            html.append("time:now,");
            html.append("requestId:data.requestId||\"\",");
            html.append("status:data.status||\"\",");
            html.append("acceptedAt:data.acceptedAt||\"\",");
            html.append("message:\"접수 완료\"");
            html.append("});");
            html.append("},function(err){");
            html.append("setSyncMessage([resolveMessage(err)]);"); 
            html.append("setBannerMessage(\"외부기관 연계 요청에 실패했습니다.\");");
            html.append("var now=nowStamp();");
            html.append("addSyncHistory(now+\" - 실패: \"+resolveMessage(err));");
            html.append("addSyncHistoryRow({");
            html.append("time:now,");
            html.append("requestId:\"\",");
            html.append("status:\"FAILED\",");
            html.append("acceptedAt:\"\",");
            html.append("message:resolveMessage(err)");
            html.append("});");
            html.append("});");
            html.append("});");
            html.append("}");
            html.append("if(document.getElementById(\"equip-body\")){");
            html.append("fetchJson(\"/api/equipments\",function(res){");
            html.append("fillTable(\"equip-body\",(res&&res.data)||[],[\"deviceId\",\"name\",\"model\",\"vendor\",\"status\"]);");
            html.append("},function(err){");
            html.append("setWarning(\"equip-warning\",err);");
            html.append("});");
            html.append("var createBtn=document.getElementById(\"equip-create\");");
            html.append("var refreshBtn=document.getElementById(\"equip-refresh\");");
            html.append("if(refreshBtn){");
            html.append("refreshBtn.addEventListener(\"click\",function(){");
            html.append("fetchJson(\"/api/equipments\",function(res){");
            html.append("fillTable(\"equip-body\",(res&&res.data)||[],[\"deviceId\",\"name\",\"model\",\"vendor\",\"status\"]);");
            html.append("setWarning(\"equip-warning\",null);");
            html.append("});");
            html.append("});");
            html.append("}");
            html.append("if(createBtn){");
            html.append("createBtn.addEventListener(\"click\",function(){");
            html.append("var payload={");
            html.append("deviceId:document.getElementById(\"equip-device\").value,");
            html.append("name:document.getElementById(\"equip-name\").value,");
            html.append("model:document.getElementById(\"equip-model\").value,");
            html.append("vendor:document.getElementById(\"equip-vendor\").value,");
            html.append("status:document.getElementById(\"equip-status\").value");
            html.append("};");
            html.append("if(!payload.name){");
            html.append("setWarning(\"equip-warning\",{errorCode:\"E-0001\",message:\"name required\"});");
            html.append("return;");
            html.append("}");
            html.append("postJson(\"/api/equipments\",payload,function(res){");
            html.append("document.getElementById(\"equip-result\").textContent=\"등록 완료\";");
            html.append("setWarning(\"equip-warning\",null);");
            html.append("fetchJson(\"/api/equipments\",function(listRes){");
            html.append("fillTable(\"equip-body\",(listRes&&listRes.data)||[],[\"deviceId\",\"name\",\"model\",\"vendor\",\"status\"]);");
            html.append("});");
            html.append("},function(err){");
            html.append("document.getElementById(\"equip-result\").textContent=\"\";");
            html.append("setWarning(\"equip-warning\",err);");
            html.append("});");
            html.append("});");
            html.append("}");
            html.append("}");
            html.append("if(document.getElementById(\"orders-body\")){");
            html.append("fetchJson(\"/api/orders\",function(res){");
            html.append("fillTable(\"orders-body\",(res&&res.data)||[],[\"orderId\",\"productCode\",\"productName\",\"quantity\",\"dueDate\",\"status\"]);");
            html.append("},function(err){");
            html.append("setWarning(\"order-warning\",err);");
            html.append("});");
            html.append("var btn=document.getElementById(\"order-search\");");
            html.append("if(btn){");
            html.append("var inputs=[\"order-id\",\"order-partner\",\"order-from\",\"order-to\",\"order-status\"]; ");
            html.append("inputs.forEach(function(id){");
            html.append("var el=document.getElementById(id);");
            html.append("if(el){el.addEventListener(\"input\",function(){");
            html.append("var msg=validateOrder();");
            html.append("setWarning(\"order-warning\",msg?{errorCode:\"\",message:msg}:null);");
            html.append("setDisabled(\"order-search\",!!msg);");
            html.append("setInvalid([\"order-from\",\"order-to\",\"order-status\"],!!msg);");
            html.append("setSummary(\"order-summary\",{");
            html.append("orderId:document.getElementById(\"order-id\").value,");
            html.append("partnerName:document.getElementById(\"order-partner\").value,");
            html.append("dueFrom:document.getElementById(\"order-from\").value,");
            html.append("dueTo:document.getElementById(\"order-to\").value,");
            html.append("status:document.getElementById(\"order-status\").value");
            html.append("});");
            html.append("});}");
            html.append("});");
            html.append("var initMsg=validateOrder();");
            html.append("setWarning(\"order-warning\",initMsg?{errorCode:\"\",message:initMsg}:null);");
            html.append("setDisabled(\"order-search\",!!initMsg);");
            html.append("setInvalid([\"order-from\",\"order-to\",\"order-status\"],!!initMsg);");
            html.append("setSummary(\"order-summary\",{");
            html.append("orderId:document.getElementById(\"order-id\").value,");
            html.append("partnerName:document.getElementById(\"order-partner\").value,");
            html.append("dueFrom:document.getElementById(\"order-from\").value,");
            html.append("dueTo:document.getElementById(\"order-to\").value,");
            html.append("status:document.getElementById(\"order-status\").value");
            html.append("});");
            html.append("btn.addEventListener(\"click\",function(){");
            html.append("var msg=validateOrder();");
            html.append("setWarning(\"order-warning\",msg?{errorCode:\"\",message:msg}:null);");
            html.append("setDisabled(\"order-search\",!!msg);");
            html.append("setInvalid([\"order-from\",\"order-to\",\"order-status\"],!!msg);");
            html.append("setSummary(\"order-summary\",{");
            html.append("orderId:document.getElementById(\"order-id\").value,");
            html.append("partnerName:document.getElementById(\"order-partner\").value,");
            html.append("dueFrom:document.getElementById(\"order-from\").value,");
            html.append("dueTo:document.getElementById(\"order-to\").value,");
            html.append("status:document.getElementById(\"order-status\").value");
            html.append("});");
            html.append("if(msg){return;}");
            html.append("var q=buildQuery({");
            html.append("orderId:document.getElementById(\"order-id\").value,");
            html.append("partnerName:document.getElementById(\"order-partner\").value,");
            html.append("dueFrom:document.getElementById(\"order-from\").value,");
            html.append("dueTo:document.getElementById(\"order-to\").value,");
            html.append("status:document.getElementById(\"order-status\").value");
            html.append("});");
            html.append("fetchJson(\"/api/orders\"+q,function(res){");
            html.append("fillTable(\"orders-body\",(res&&res.data)||[],[\"orderId\",\"productCode\",\"productName\",\"quantity\",\"dueDate\",\"status\"]);");
            html.append("},function(err){");
            html.append("setWarning(\"order-warning\",err);");
            html.append("});");
            html.append("});");
            html.append("}");
            html.append("}");
            html.append("var orderCreateBtn=document.getElementById(\"order-create\");");
            html.append("if(orderCreateBtn){");
            html.append("orderCreateBtn.addEventListener(\"click\",function(){");
            html.append("var payload={");
            html.append("orderNo:document.getElementById(\"order-create-no\").value,");
            html.append("partnerName:document.getElementById(\"order-create-partner\").value,");
            html.append("dueDate:document.getElementById(\"order-create-due\").value,");
            html.append("status:document.getElementById(\"order-create-status\").value");
            html.append("};");
            html.append("if(!payload.orderNo){");
            html.append("setWarning(\"order-create-warning\",{errorCode:\"E-0001\",message:\"orderNo required\"});");
            html.append("return;");
            html.append("}");
            html.append("if(payload.dueDate && !isValidDate(payload.dueDate)){");
            html.append("setWarning(\"order-create-warning\",{errorCode:\"E-1001\",message:\"invalid date format\"});");
            html.append("return;");
            html.append("}");
            html.append("postJson(\"/api/orders\",payload,function(res){");
            html.append("document.getElementById(\"order-create-result\").textContent=\"등록 완료\";");
            html.append("setWarning(\"order-create-warning\",null);");
            html.append("fetchJson(\"/api/orders\",function(listRes){");
            html.append("fillTable(\"orders-body\",(listRes&&listRes.data)||[],[\"orderId\",\"productCode\",\"productName\",\"quantity\",\"dueDate\",\"status\"]);");
            html.append("});");
            html.append("},function(err){");
            html.append("document.getElementById(\"order-create-result\").textContent=\"\";");
            html.append("setWarning(\"order-create-warning\",err);");
            html.append("});");
            html.append("});");
            html.append("}");
            html.append("if(document.getElementById(\"jobs-body\")){");
            html.append("fetchJson(\"/api/jobs\",function(res){");
            html.append("fillTable(\"jobs-body\",(res&&res.data)||[],[\"jobId\",\"orderId\",\"processName\",\"startAt\",\"endAt\",\"status\"]);");
            html.append("},function(err){");
            html.append("setWarning(\"job-warning\",err);");
            html.append("});");
            html.append("var btn=document.getElementById(\"job-search\");");
            html.append("if(btn){");
            html.append("var inputs=[\"job-id\",\"job-order\",\"job-process\",\"job-from\",\"job-to\",\"job-status\"]; ");
            html.append("inputs.forEach(function(id){");
            html.append("var el=document.getElementById(id);");
            html.append("if(el){el.addEventListener(\"input\",function(){");
            html.append("var msg=validateJob();");
            html.append("setWarning(\"job-warning\",msg?{errorCode:\"\",message:msg}:null);");
            html.append("setDisabled(\"job-search\",!!msg);");
            html.append("setInvalid([\"job-from\",\"job-to\",\"job-status\"],!!msg);");
            html.append("setSummary(\"job-summary\",{");
            html.append("jobId:document.getElementById(\"job-id\").value,");
            html.append("orderId:document.getElementById(\"job-order\").value,");
            html.append("processName:document.getElementById(\"job-process\").value,");
            html.append("from:document.getElementById(\"job-from\").value,");
            html.append("to:document.getElementById(\"job-to\").value,");
            html.append("status:document.getElementById(\"job-status\").value");
            html.append("});");
            html.append("});}");
            html.append("});");
            html.append("var initMsg=validateJob();");
            html.append("setWarning(\"job-warning\",initMsg?{errorCode:\"\",message:initMsg}:null);");
            html.append("setDisabled(\"job-search\",!!initMsg);");
            html.append("setInvalid([\"job-from\",\"job-to\",\"job-status\"],!!initMsg);");
            html.append("setSummary(\"job-summary\",{");
            html.append("jobId:document.getElementById(\"job-id\").value,");
            html.append("orderId:document.getElementById(\"job-order\").value,");
            html.append("processName:document.getElementById(\"job-process\").value,");
            html.append("from:document.getElementById(\"job-from\").value,");
            html.append("to:document.getElementById(\"job-to\").value,");
            html.append("status:document.getElementById(\"job-status\").value");
            html.append("});");
            html.append("btn.addEventListener(\"click\",function(){");
            html.append("var msg=validateJob();");
            html.append("setWarning(\"job-warning\",msg?{errorCode:\"\",message:msg}:null);");
            html.append("setDisabled(\"job-search\",!!msg);");
            html.append("setInvalid([\"job-from\",\"job-to\",\"job-status\"],!!msg);");
            html.append("setSummary(\"job-summary\",{");
            html.append("jobId:document.getElementById(\"job-id\").value,");
            html.append("orderId:document.getElementById(\"job-order\").value,");
            html.append("processName:document.getElementById(\"job-process\").value,");
            html.append("from:document.getElementById(\"job-from\").value,");
            html.append("to:document.getElementById(\"job-to\").value,");
            html.append("status:document.getElementById(\"job-status\").value");
            html.append("});");
            html.append("if(msg){return;}");
            html.append("var q=buildQuery({");
            html.append("jobId:document.getElementById(\"job-id\").value,");
            html.append("orderId:document.getElementById(\"job-order\").value,");
            html.append("processName:document.getElementById(\"job-process\").value,");
            html.append("from:document.getElementById(\"job-from\").value,");
            html.append("to:document.getElementById(\"job-to\").value,");
            html.append("status:document.getElementById(\"job-status\").value");
            html.append("});");
            html.append("fetchJson(\"/api/jobs\"+q,function(res){");
            html.append("fillTable(\"jobs-body\",(res&&res.data)||[],[\"jobId\",\"orderId\",\"processName\",\"startAt\",\"endAt\",\"status\"]);");
            html.append("},function(err){");
            html.append("setWarning(\"job-warning\",err);");
            html.append("});");
            html.append("});");
            html.append("}");
            html.append("}");
            html.append("var jobCreateBtn=document.getElementById(\"job-create\");");
            html.append("if(jobCreateBtn){");
            html.append("jobCreateBtn.addEventListener(\"click\",function(){");
            html.append("var payload={");
            html.append("orderId:document.getElementById(\"job-create-order\").value,");
            html.append("processName:document.getElementById(\"job-create-process\").value,");
            html.append("startAt:document.getElementById(\"job-create-start\").value,");
            html.append("endAt:document.getElementById(\"job-create-end\").value,");
            html.append("status:document.getElementById(\"job-create-status\").value");
            html.append("};");
            html.append("if(!payload.orderId||!payload.processName){");
            html.append("setWarning(\"job-create-warning\",{errorCode:\"E-0001\",message:\"orderId and processName required\"});");
            html.append("return;");
            html.append("}");
            html.append("if(payload.startAt && !/^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$/.test(payload.startAt)){");
            html.append("setWarning(\"job-create-warning\",{errorCode:\"E-1001\",message:\"invalid date format\"});");
            html.append("return;");
            html.append("}");
            html.append("if(payload.endAt && !/^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$/.test(payload.endAt)){");
            html.append("setWarning(\"job-create-warning\",{errorCode:\"E-1001\",message:\"invalid date format\"});");
            html.append("return;");
            html.append("}");
            html.append("postJson(\"/api/jobs\",payload,function(res){");
            html.append("document.getElementById(\"job-create-result\").textContent=\"등록 완료\";");
            html.append("setWarning(\"job-create-warning\",null);");
            html.append("fetchJson(\"/api/jobs\",function(listRes){");
            html.append("fillTable(\"jobs-body\",(listRes&&listRes.data)||[],[\"jobId\",\"orderId\",\"processName\",\"startAt\",\"endAt\",\"status\"]);");
            html.append("});");
            html.append("},function(err){");
            html.append("document.getElementById(\"job-create-result\").textContent=\"\";");
            html.append("setWarning(\"job-create-warning\",err);");
            html.append("});");
            html.append("});");
            html.append("}");
            html.append("if(document.getElementById(\"kpi-body\")){");
            html.append("fetchJson(\"/api/kpi\",function(res){");
            html.append("fillTable(\"kpi-body\",(res&&res.data)||[],[\"name\",\"targetValue\",\"currentValue\",\"progressRate\",\"resultValue\",\"unit\"]);");
            html.append("},function(err){");
            html.append("setWarning(\"kpi-warning\",err);");
            html.append("});");
            html.append("var btn=document.getElementById(\"kpi-search\");");
            html.append("if(btn){");
            html.append("var inputs=[\"kpi-name\",\"kpi-id\",\"kpi-from\",\"kpi-to\"]; ");
            html.append("inputs.forEach(function(id){");
            html.append("var el=document.getElementById(id);");
            html.append("if(el){el.addEventListener(\"input\",function(){");
            html.append("var msg=validateKpi();");
            html.append("setWarning(\"kpi-warning\",msg?{errorCode:\"\",message:msg}:null);");
            html.append("setDisabled(\"kpi-search\",!!msg);");
            html.append("setInvalid([\"kpi-from\",\"kpi-to\"],!!msg);");
            html.append("setSummary(\"kpi-summary\",{");
            html.append("name:document.getElementById(\"kpi-name\").value,");
            html.append("kpiId:document.getElementById(\"kpi-id\").value,");
            html.append("from:document.getElementById(\"kpi-from\").value,");
            html.append("to:document.getElementById(\"kpi-to\").value");
            html.append("});");
            html.append("});}");
            html.append("});");
            html.append("var initMsg=validateKpi();");
            html.append("setWarning(\"kpi-warning\",initMsg?{errorCode:\"\",message:initMsg}:null);");
            html.append("setDisabled(\"kpi-search\",!!initMsg);");
            html.append("setInvalid([\"kpi-from\",\"kpi-to\"],!!initMsg);");
            html.append("setSummary(\"kpi-summary\",{");
            html.append("name:document.getElementById(\"kpi-name\").value,");
            html.append("kpiId:document.getElementById(\"kpi-id\").value,");
            html.append("from:document.getElementById(\"kpi-from\").value,");
            html.append("to:document.getElementById(\"kpi-to\").value");
            html.append("});");
            html.append("btn.addEventListener(\"click\",function(){");
            html.append("var msg=validateKpi();");
            html.append("setWarning(\"kpi-warning\",msg?{errorCode:\"\",message:msg}:null);");
            html.append("setDisabled(\"kpi-search\",!!msg);");
            html.append("setInvalid([\"kpi-from\",\"kpi-to\"],!!msg);");
            html.append("setSummary(\"kpi-summary\",{");
            html.append("name:document.getElementById(\"kpi-name\").value,");
            html.append("kpiId:document.getElementById(\"kpi-id\").value,");
            html.append("from:document.getElementById(\"kpi-from\").value,");
            html.append("to:document.getElementById(\"kpi-to\").value");
            html.append("});");
            html.append("if(msg){return;}");
            html.append("var q=buildQuery({");
            html.append("name:document.getElementById(\"kpi-name\").value,");
            html.append("kpiId:document.getElementById(\"kpi-id\").value,");
            html.append("from:document.getElementById(\"kpi-from\").value,");
            html.append("to:document.getElementById(\"kpi-to\").value");
            html.append("});");
            html.append("fetchJson(\"/api/kpi\"+q,function(res){");
            html.append("fillTable(\"kpi-body\",(res&&res.data)||[],[\"name\",\"targetValue\",\"currentValue\",\"progressRate\",\"resultValue\",\"unit\"]);");
            html.append("},function(err){");
            html.append("setWarning(\"kpi-warning\",err);");
            html.append("});");
            html.append("fetchJson(\"/api/kpi/trend\"+q,function(res){");
            html.append("var rows=(res&&res.data)||[];");
            html.append("fillTable(\"kpi-trend-body\",rows,[\"date\",\"targetValue\",\"currentValue\"]);");
            html.append("renderKpiChart(rows);");
            html.append("},function(err){");
            html.append("setWarning(\"kpi-warning\",err);");
            html.append("});");
            html.append("});");
            html.append("}");
            html.append("}");
            html.append("var kpiCreateBtn=document.getElementById(\"kpi-create\");");
            html.append("if(kpiCreateBtn){");
            html.append("kpiCreateBtn.addEventListener(\"click\",function(){");
            html.append("var payload={");
            html.append("name:document.getElementById(\"kpi-create-name\").value,");
            html.append("targetValue:document.getElementById(\"kpi-create-target\").value,");
            html.append("currentValue:document.getElementById(\"kpi-create-current\").value,");
            html.append("unit:document.getElementById(\"kpi-create-unit\").value,");
            html.append("formula:document.getElementById(\"kpi-create-formula\").value");
            html.append("};");
            html.append("if(!payload.name){");
            html.append("setWarning(\"kpi-create-warning\",{errorCode:\"E-0001\",message:\"name required\"});");
            html.append("return;");
            html.append("}");
            html.append("if(payload.targetValue && isNaN(Number(payload.targetValue))){");
            html.append("setWarning(\"kpi-create-warning\",{errorCode:\"E-1004\",message:\"invalid numeric value\"});");
            html.append("return;");
            html.append("}");
            html.append("if(payload.currentValue && isNaN(Number(payload.currentValue))){");
            html.append("setWarning(\"kpi-create-warning\",{errorCode:\"E-1004\",message:\"invalid numeric value\"});");
            html.append("return;");
            html.append("}");
            html.append("postJson(\"/api/kpi\",payload,function(res){");
            html.append("document.getElementById(\"kpi-create-result\").textContent=\"등록 완료\";");
            html.append("setWarning(\"kpi-create-warning\",null);");
            html.append("fetchJson(\"/api/kpi\",function(listRes){");
            html.append("fillTable(\"kpi-body\",(listRes&&listRes.data)||[],[\"name\",\"targetValue\",\"currentValue\",\"progressRate\",\"resultValue\",\"unit\"]);");
            html.append("});");
            html.append("},function(err){");
            html.append("document.getElementById(\"kpi-create-result\").textContent=\"\";");
            html.append("setWarning(\"kpi-create-warning\",err);");
            html.append("});");
            html.append("});");
            html.append("}");
            html.append("if(document.getElementById(\"kpi-trend-body\")){");
            html.append("fetchJson(\"/api/kpi/trend\",function(res){");
            html.append("var rows=(res&&res.data)||[];");
            html.append("fillTable(\"kpi-trend-body\",rows,[\"date\",\"targetValue\",\"currentValue\"]);");
            html.append("renderKpiChart(rows);");
            html.append("},function(err){");
            html.append("setWarning(\"kpi-warning\",err);");
            html.append("});");
            html.append("}");
            html.append("if(document.getElementById(\"sync-log-body\")){");
            html.append("var syncSearch=document.getElementById(\"sync-search\");");
            html.append("var syncReset=document.getElementById(\"sync-reset\");");
            html.append("function syncValidate(){");
            html.append("var from=document.getElementById(\"sync-from\").value;");
            html.append("var to=document.getElementById(\"sync-to\").value;");
            html.append("var status=document.getElementById(\"sync-status\").value;");
            html.append("if((from&&!to)||(!from&&to)){return \"기간 시작/종료는 함께 입력해야 합니다.\";}");
            html.append("if(!isValidDate(from)||!isValidDate(to)){return \"날짜 형식이 올바르지 않습니다. YYYY-MM-DD로 입력하세요.\";}");
            html.append("if(!isValidDateRange(from,to)){return \"기간 시작이 종료보다 늦을 수 없습니다.\";}");
            html.append("if(!isValidSyncStatus(status)){return \"상태 값은 ACCEPTED 또는 FAILED만 가능합니다.\";}");
            html.append("return \"\";");
            html.append("}");
            html.append("function syncSetWarning(msg){");
            html.append("var el=document.getElementById(\"sync-warning\");");
            html.append("if(el){el.textContent=msg||\"\";}");
            html.append("}");
            html.append("if(syncReset){");
            html.append("syncReset.addEventListener(\"click\",function(){");
            html.append("document.getElementById(\"sync-from\").value=\"\";");
            html.append("document.getElementById(\"sync-to\").value=\"\";");
            html.append("document.getElementById(\"sync-status\").value=\"\";");
            html.append("syncSetWarning(\"\");");
            html.append("});");
            html.append("}");
            html.append("if(syncSearch){");
            html.append("syncSearch.addEventListener(\"click\",function(){");
            html.append("var msg=syncValidate();");
            html.append("syncSetWarning(msg);");
            html.append("if(msg){return;}");
            html.append("document.getElementById(\"sync-log-body\").innerHTML=\"<tr><td colspan=\\\"5\\\" style=\\\"padding:4px 0;\\\">조회 완료(임시)</td></tr>\";");
            html.append("});");
            html.append("}");
            html.append("}");
            html.append("})();");
            html.append("</script>");
        }
        // 페이지 하단 제목/경로 영역.
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
    // 입력: 원본문자열(null 가능).
    // 출력: HTML 안전 문자열.
    private static String escape(String value) {
        // 초보자 설명:
        // - 사용자가 입력한 값을 그대로 HTML에 넣으면 보안 문제가 생길 수 있다.
        // - 그래서 특수문자를 안전한 문자로 바꿔준다.
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
    }
}
