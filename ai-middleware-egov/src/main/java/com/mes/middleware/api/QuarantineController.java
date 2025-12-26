package com.mes.middleware.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuarantineController {
    private static final String QUARANTINE_DIR = "data/quarantine";

    // 목적: 격리된 원본 목록을 조회한다.
    // 이유: P1 단계에서 격리 상태를 확인할 최소 조회 기능이 필요하다.
    @GetMapping("/api/quarantine")
    public ResponseEntity<Map<String, Object>> list() {
        try {
            Path dir = Paths.get(QUARANTINE_DIR);
            if (!Files.exists(dir)) {
                return ResponseEntity.ok(ok(List.of()));
            }
            List<String> files = Files.list(dir)
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ok(files));
        } catch (IOException ex) {
            return ResponseEntity.ok(fail("E-0001", "quarantine list failed"));
        }
    }

    private Map<String, Object> ok(Object data) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("result", "OK");
        res.put("message", "");
        res.put("data", data);
        return res;
    }

    private Map<String, Object> fail(String errorCode, String message) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("result", "FAIL");
        res.put("message", message);
        res.put("errorCode", errorCode);
        res.put("data", null);
        return res;
    }
}
