package com.mes.web.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.mes.web.api.ApiController;
import com.mes.web.db.DbSupport;
import com.mes.web.health.HealthController;
import com.mes.web.ui.UiController;
import com.mes.web.ui.UiRouteController;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.mes.web")
public class WebConfig {
    // 초보자 설명:
    // - 이 클래스는 "스프링이 어떤 구성요소를 실행할지"를 알려주는 설정 파일이다.
    // - @Bean으로 등록된 클래스는 스프링이 자동으로 생성해서 관리한다.
    // - 따라서 여기서 등록된 컨트롤러는 앱 시작 시 바로 사용할 수 있다.
    // HealthController를 명시적으로 등록한다.
    // 목적: 헬스체크 엔드포인트를 확실히 노출한다.
    // 이유: 초기 단계에서는 자동 스캔이 실패해도 항상 헬스체크가 동작하도록 보장한다.
    @Bean
    public HealthController healthController() {
        // 헬스체크는 서비스가 살아있는지 확인하는 기본 경로다.
        return new HealthController();
    }

    // ApiController를 명시적으로 등록한다.
    // 목적: 최소 API 묶음을 강제로 등록한다.
    // 이유: 스모크 스크립트는 API가 항상 존재한다고 가정하기 때문이다.
    @Bean
    public ApiController apiController() {
        // ApiController는 DB 사용/미사용을 자동으로 분기해야 한다.
        // 그래서 DbSupport를 주입해준다.
        return new ApiController(dbSupport());
    }

    // DB 접속 정보가 있을 때만 JDBC를 활성화한다.
    @Bean
    public DbSupport dbSupport() {
        // DB 접속 정보가 있을 때만 활성화된다(환경 변수 기반).
        return new DbSupport();
    }

    // UiController를 명시적으로 등록한다.
    // 목적: UI 홈(/ui)을 확실히 등록한다.
    // 이유: 스캐폴딩 단계에서 UI 진입점이 없으면 작업을 진행할 수 없다.
    @Bean
    public UiController uiController() {
        // UI의 기본 진입점(/ui)을 제공하는 컨트롤러다.
        return new UiController();
    }

    // UiRouteController를 명시적으로 등록한다.
    // 목적: /ui 하위 라우팅을 안정적으로 등록한다.
    // 이유: 화면 개수가 많아도 라우팅이 실패하지 않도록 안전장치를 둔다.
    @Bean
    public UiRouteController uiRouteController() {
        // 실제 화면 주소(/ui/...)를 공통 템플릿으로 연결한다.
        return new UiRouteController();
    }
}
