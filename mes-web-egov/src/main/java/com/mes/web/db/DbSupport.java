package com.mes.web.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

// 목적: DB 연결이 준비된 경우에만 JDBC를 활성화한다.
// 이유: 접속 정보가 없으면 스캐폴딩 흐름(메모리 저장소)이 깨지지 않아야 하기 때문이다.
// 초보자 설명:
// - 이 클래스는 "DB가 준비됐는지"를 판단하는 스위치 역할을 한다.
// - 환경 변수(또는 JVM 옵션)로 DB 정보가 들어오면 DB 모드로 전환된다.
// - 정보가 없으면 DB를 쓰지 않고 메모리 모드로 동작한다.
public class DbSupport {
    private final JdbcTemplate jdbcTemplate;
    private final boolean enabled;

    public DbSupport() {
        // 초보자 설명:
        // - DB 접속 정보는 "코드에 직접 쓰지 않고" 외부에서 주입한다.
        // - sysKey: JVM 옵션(-D), envKey: 운영 환경 변수 값을 의미한다.
        String url = pick("mes.db.url", "MES_DB_URL");
        String user = pick("mes.db.user", "MES_DB_USER");
        String pass = pick("mes.db.password", "MES_DB_PASSWORD");

        // 접속 정보가 하나라도 비어있으면 DB를 사용하지 않는다.
        if (isBlank(url) || isBlank(user) || isBlank(pass)) {
            this.jdbcTemplate = null;
            this.enabled = false;
            return;
        }

        // 초보자 설명:
        // - DriverManagerDataSource는 가장 단순한 DB 연결 방식이다.
        // - 설정이 간단해 스캐폴딩 단계에 적합하다.
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.mariadb.jdbc.Driver");
        ds.setUrl(url);
        ds.setUsername(user);
        ds.setPassword(pass);
        this.jdbcTemplate = new JdbcTemplate(ds);
        this.enabled = true;
    }

    public boolean isEnabled() {
        // DB 사용 가능 여부를 외부에 알려준다.
        return enabled;
    }

    public JdbcTemplate jdbc() {
        // 실제 DB 접근 객체를 반환한다.
        return jdbcTemplate;
    }

    private String pick(String sysKey, String envKey) {
        // 우선순위: JVM 옵션 > 환경 변수
        String sys = System.getProperty(sysKey);
        if (!isBlank(sys)) {
            return sys;
        }
        return System.getenv(envKey);
    }

    private boolean isBlank(String value) {
        // 공백 문자열도 빈 값으로 처리한다.
        return value == null || value.trim().isEmpty();
    }
}
