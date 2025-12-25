package com.mes.web.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.mes.web.api.ApiController;
import com.mes.web.health.HealthController;
import com.mes.web.ui.UiController;
import com.mes.web.ui.UiRouteController;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.mes.web")
public class WebConfig {
    // HealthController를 명시적으로 등록한다.
    // 이유: 초기 단계에서는 자동 스캔이 실패해도 항상 헬스체크가 동작하도록 보장한다.
    @Bean
    public HealthController healthController() {
        return new HealthController();
    }

    // ApiController를 명시적으로 등록한다.
    // 이유: 최소 API가 반드시 살아 있어야 스모크가 통과하므로 안전장치를 둔다.
    @Bean
    public ApiController apiController() {
        return new ApiController();
    }

    // UiController를 명시적으로 등록한다.
    // 이유: UI 스캐폴딩 라우팅(/ui)을 스모크 기준으로 제공하기 위함이다.
    @Bean
    public UiController uiController() {
        return new UiController();
    }

    // UiRouteController를 명시적으로 등록한다.
    // 이유: /ui 하위 라우팅을 스모크 기준으로 제공하기 위함이다.
    @Bean
    public UiRouteController uiRouteController() {
        return new UiRouteController();
    }
}
