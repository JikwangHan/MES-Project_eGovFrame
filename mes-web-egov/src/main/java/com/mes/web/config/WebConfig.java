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
    // 목적: 헬스체크 엔드포인트를 확실히 노출한다.
    // 이유: 초기 단계에서는 자동 스캔이 실패해도 항상 헬스체크가 동작하도록 보장한다.
    @Bean
    public HealthController healthController() {
        return new HealthController();
    }

    // ApiController를 명시적으로 등록한다.
    // 목적: 최소 API 묶음을 강제로 등록한다.
    // 이유: 스모크 스크립트는 API가 항상 존재한다고 가정하기 때문이다.
    @Bean
    public ApiController apiController() {
        return new ApiController();
    }

    // UiController를 명시적으로 등록한다.
    // 목적: UI 홈(/ui)을 확실히 등록한다.
    // 이유: 스캐폴딩 단계에서 UI 진입점이 없으면 작업을 진행할 수 없다.
    @Bean
    public UiController uiController() {
        return new UiController();
    }

    // UiRouteController를 명시적으로 등록한다.
    // 목적: /ui 하위 라우팅을 안정적으로 등록한다.
    // 이유: 화면 개수가 많아도 라우팅이 실패하지 않도록 안전장치를 둔다.
    @Bean
    public UiRouteController uiRouteController() {
        return new UiRouteController();
    }
}
