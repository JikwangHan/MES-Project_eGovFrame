package com.mes.web.ui;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LocalUiBypassFilter implements Filter {
    // -------------------------------------------------------------
    // 로컬 UI 우회 필터
    // 목적: 로컬 테스트에서 /ui 경로 접근을 막는 403을 우회한다.
    // 이유: 인증 구현 전에도 화면 확인(스모크)을 수행해야 하기 때문이다.
    // -------------------------------------------------------------
    // 초보자 설명:
    // - "local" 프로파일에서만 동작한다.
    // - /ui 요청을 가로채서 바로 HTML을 반환한다.
    // - 운영 환경에는 영향을 주지 않는다.
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestUri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestUri;
        if (contextPath != null && !contextPath.isEmpty() && requestUri.startsWith(contextPath)) {
            path = requestUri.substring(contextPath.length());
        }
        if (!"GET".equalsIgnoreCase(httpRequest.getMethod()) || !path.startsWith("/ui")) {
            chain.doFilter(request, response);
            return;
        }
        // 목적: /ui 요청은 컨트롤러 대신 공통 템플릿으로 즉시 응답한다.
        // 이유: 보안 필터가 403을 반환하더라도 화면 골격 검증은 가능해야 한다.
        String description = "/ui".equals(path)
            ? "화면 골격 준비 완료. 데이터 바인딩은 후속 단계에서 적용합니다."
            : UiRouteDescriptions.describe(path);
        String html = UiPageTemplate.render(
            "MES UI",
            "MES UI 스캐폴딩",
            description,
            path
        );
        httpResponse.setStatus(HttpServletResponse.SC_OK);
        httpResponse.setContentType("text/html; charset=UTF-8");
        httpResponse.getWriter().write(html);
    }
}
