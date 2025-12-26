package com.mes.web.embedded;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.mes.web.config.WebConfig;

public class EmbeddedTomcat {
    // 외장 Tomcat 배포가 어려운 환경에서도 동일한 스캐폴딩을 검증하기 위한 실행 진입점이다.
    // 목적: 별도의 WAS 설치 없이도 로컬 실행이 가능하도록 한다.
    // 이유: 개발 초기 단계에서는 빠른 확인이 더 중요하기 때문이다.
    public static void main(String[] args) throws Exception {
        // 기본 포트는 18080이며, 운영 환경에서 충돌을 피하려면 server.port로 변경한다.
        int port = Integer.parseInt(System.getProperty("server.port", "18080"));

        // 내장 Tomcat을 수동 구성한다.
        // 이유: 외장 Tomcat 없이도 로컬에서 동일한 구조를 검증할 수 있어 초기 개발 속도가 빨라진다.
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector();

        File baseDir = new File(".");
        // 컨텍스트 루트를 빈 문자열로 두어 /health, /api/* 경로를 그대로 사용한다.
        Context context = tomcat.addContext("", baseDir.getAbsolutePath());

        // Java Config 기반 컨텍스트로 실행하여 실행 위치와 무관하게 동작하도록 한다.
        AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
        appContext.setServletContext(context.getServletContext());
        appContext.register(WebConfig.class);
        appContext.refresh();

        // 모든 요청을 컨트롤러로 전달하는 DispatcherServlet을 등록한다.
        DispatcherServlet dispatcher = new DispatcherServlet(appContext);
        Tomcat.addServlet(context, "dispatcher", dispatcher).setLoadOnStartup(1);
        context.addServletMappingDecoded("/*", "dispatcher");

        // 서버가 종료되지 않도록 대기하여 백그라운드 서비스처럼 유지한다.
        tomcat.start();
        tomcat.getServer().await();
    }
}
