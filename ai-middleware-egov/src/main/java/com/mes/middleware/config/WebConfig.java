package com.mes.middleware.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.mes.middleware")
public class WebConfig {
    // Java Config 기반으로 컨트롤러를 자동 스캔한다.
    // 이유: XML 없이도 설정을 유지할 수 있어 향후 변경에 유연하다.
}
