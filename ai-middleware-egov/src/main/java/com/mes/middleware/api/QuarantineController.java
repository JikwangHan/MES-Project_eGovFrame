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
    // 초보자 설명:
    // - 격리된 원본 파일 목록을 조회하는 API다.
    // - 자동 파싱이 실패한 데이터를 사람이 확인할 수 있다.
    // 격리 데이터를 보관하는 폴더(상대 경로).
    // 이유: 장비/프로토콜을 판단하지 못한 원본을 따로 관리해야 재처리가 가능하다.
    private static final String QUARANTINE_DIR = "data/quarantine";

    // 목적: 격리된 원본 목록을 조회한다.
    // 이유: P1 단계에서 격리 상태를 확인할 최소 조회 기능이 필요하다.
    // 입력: 없음.
    // 출력: 격리 파일명 목록.
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

    // 공통 성공 응답 포맷.
    private Map<String, Object> ok(Object data) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("result", "OK");
        res.put("message", "");
        res.put("data", data);
        return res;
    }

    // 공통 실패 응답 포맷.
    private Map<String, Object> fail(String errorCode, String message) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("result", "FAIL");
        res.put("message", message);
        res.put("errorCode", errorCode);
        res.put("data", null);
        return res;
    }
}
