package com.mes.middleware;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.mes.middleware.config.WebConfig;

public class EmbeddedTomcat {
    public static void main(String[] args) throws Exception {
        // 기본 포트는 18081이며, 운영 환경에 맞게 server.port로 변경할 수 있다.
        int port = Integer.parseInt(System.getProperty("server.port", "18081"));

        // 내장 Tomcat으로 미들웨어를 독립 실행한다.
        // 이유: MES Web과 분리된 서비스로 운영할 수 있도록 하기 위함이다.
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector();

        File baseDir = new File(".");
        Context context = tomcat.addContext("", baseDir.getAbsolutePath());

        AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
        appContext.setServletContext(context.getServletContext());
        appContext.register(WebConfig.class);
        appContext.refresh();

        DispatcherServlet dispatcher = new DispatcherServlet(appContext);
        Tomcat.addServlet(context, "dispatcher", dispatcher).setLoadOnStartup(1);
        context.addServletMappingDecoded("/*", "dispatcher");

        tomcat.start();
        tomcat.getServer().await();
    }
}
