package com.mes.middleware.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.mes.middleware")
public class WebConfig {
    // 초보자 설명:
    // - 이 설정은 "미들웨어 서버가 어떤 클래스들을 읽을지" 알려준다.
    // - @ComponentScan 덕분에 컨트롤러/서비스가 자동으로 등록된다.
    // Java Config 기반으로 컨트롤러를 자동 스캔한다.
    // 이유: XML 없이도 설정을 유지할 수 있어 향후 변경에 유연하다.
    // 구성: com.mes.middleware 하위의 @RestController/@Service를 모두 스캔한다.
}
