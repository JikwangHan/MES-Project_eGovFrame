package com.mes.middleware.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mes.middleware.pipeline.RawPipelineService;

@RestController
public class RawIngestController {
    // 원본 데이터를 파일로 보관할 폴더명(상대 경로).
    // 이유: 외부 경로를 고정하지 않으면 운영 환경마다 경로가 달라져 관리가 어렵다.
    private static final String RAW_DIR = "data/raw";

    private final RawPipelineService pipelineService;

    public RawIngestController(RawPipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @PostMapping("/api/raw-ingest")
    public ResponseEntity<Map<String, Object>> rawIngest(@RequestBody String payload) {
        try {
            String id = storeRaw(payload == null ? "" : payload);
            pipelineService.process(id, payload);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", id);
            return ResponseEntity.status(HttpStatus.CREATED).body(ok(data));
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(fail("E-0000", "raw store failed"));
        }
    }

    // 원본 데이터를 파일로 저장하고 식별자를 반환한다.
    // 이유: 원본을 불변으로 보관해 재처리와 감사에 사용하기 위함이다.
    private String storeRaw(String payload) throws IOException {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String id = ts + "-" + UUID.randomUUID();
        Path dir = Paths.get(RAW_DIR);
        Files.createDirectories(dir);
        Path file = dir.resolve(id + ".raw");
        Files.writeString(file, payload, StandardCharsets.UTF_8);
        return id;
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
