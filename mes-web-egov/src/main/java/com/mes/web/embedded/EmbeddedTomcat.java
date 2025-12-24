package com.mes.web.embedded;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class EmbeddedTomcat {
    // 외장 Tomcat 배포가 어려운 환경에서도 동일한 스캐폴딩을 검증하기 위한 실행 진입점이다.
    public static void main(String[] args) throws Exception {
        // 기본 포트는 18080이며, 운영 환경에서 충돌을 피하려면 server.port로 변경한다.
        int port = Integer.parseInt(System.getProperty("server.port", "18080"));

        // eGovFrame의 Spring MVC 설정을 그대로 사용하기 위해 내장 Tomcat을 수동 구성한다.
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector();

        File baseDir = new File(".");
        Context context = tomcat.addContext("", baseDir.getAbsolutePath());

        // XML 기반 컨텍스트를 로딩하여 운영/외장 Tomcat과 동일한 설정을 공유한다.
        XmlWebApplicationContext appContext = new XmlWebApplicationContext();
        appContext.setConfigLocation("classpath:/spring/servlet-context.xml");

        DispatcherServlet dispatcher = new DispatcherServlet(appContext);
        Tomcat.addServlet(context, "dispatcher", dispatcher).setLoadOnStartup(1);
        context.addServletMappingDecoded("/", "dispatcher");

        tomcat.start();
        tomcat.getServer().await();
    }
}
