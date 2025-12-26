package com.mes.middleware.health;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    // 목적: 미들웨어가 정상 기동 중인지 빠르게 확인할 수 있는 헬스 체크를 제공한다.
    // 이유: 스모크/운영 자동화에서 가장 먼저 확인하는 기준이 200 응답이기 때문이다.
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("status", "OK");
        return res;
    }
}
