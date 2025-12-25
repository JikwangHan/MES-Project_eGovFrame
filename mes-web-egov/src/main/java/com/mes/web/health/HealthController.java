package com.mes.web.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    // 서버 기동 여부를 가장 단순하게 확인하는 엔드포인트이다.
    // 이유: 운영/스모크 스크립트는 복잡한 검증보다 "응답이 200인가"만 확인하는 것이 안정적이다.
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
