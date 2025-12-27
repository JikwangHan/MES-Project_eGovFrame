package com.mes.web.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    // 초보자 설명:
    // - 이 경로는 "서비스가 살아있는지"만 확인한다.
    // - 복잡한 로직 없이 200 OK가 오면 정상이다.
    // 서버 기동 여부를 가장 단순하게 확인하는 엔드포인트이다.
    // 이유: 운영/스모크 스크립트는 복잡한 검증보다 "응답이 200인가"만 확인하는 것이 안정적이다.
    // 입력: 없음.
    // 출력: 문자열 "OK" + 200.
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
