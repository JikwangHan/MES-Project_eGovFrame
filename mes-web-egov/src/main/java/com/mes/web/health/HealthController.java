package com.mes.web.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    // 서버 기동 여부를 가장 단순하게 확인하는 엔드포인트이다.
    // CI/스모크 스크립트가 200 응답만 확인하도록 설계한다.
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
