package com.mes.web.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.mes.web.api.ApiController;
import com.mes.web.health.HealthController;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.mes.web")
public class WebConfig {
    @Bean
    public HealthController healthController() {
        return new HealthController();
    }

    @Bean
    public ApiController apiController() {
        return new ApiController();
    }
}
