package com.mes.middleware.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mes.middleware.pipeline.QuarantineReprocessService;
import com.mes.middleware.pipeline.QuarantineReprocessService.RetryPolicy;

@RestController
public class QuarantineReprocessController {
    // 목적: 격리 데이터 재처리 요청을 받아 흐름을 연결한다.
    // 이유: 재처리 요청은 API로만 일관되게 처리해야 운영이 단순해진다.
    private final QuarantineReprocessService reprocessService;

    public QuarantineReprocessController(QuarantineReprocessService reprocessService) {
        this.reprocessService = reprocessService;
    }

    // 목적: 격리 데이터 재처리 요청을 수행한다.
    // 이유: 운영자가 재처리 결과를 즉시 확인할 수 있어야 하기 때문이다.
    @PostMapping("/api/quarantine/reprocess")
    public ResponseEntity<Map<String, Object>> reprocess(@RequestBody Map<String, String> body) {
        String rawId = body == null ? null : body.get("rawId");
        QuarantineReprocessService.ReprocessResult result = reprocessService.reprocess(rawId);
        if (!result.success) {
            HttpStatus status = "NOT_FOUND".equals(result.code)
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(fail(result.code, result.reason));
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("decision", result.decision);
        data.put("reason", result.reason);
        data.put("summary", result.summary);
        return ResponseEntity.ok(ok(data));
    }

    // 목적: 재시도 정책을 적용해 재처리를 수행한다.
    // 이유: 재시도 조건(횟수/간격/상태)을 API에서 통제하기 위함이다.
    @PostMapping("/api/quarantine/reprocess/retry")
    public ResponseEntity<Map<String, Object>> retry(@RequestBody Map<String, String> body) {
        String rawId = body == null ? null : body.get("rawId");
        int maxAttempts = parseInt(body == null ? null : body.get("maxAttempts"), 1);
        int intervalSeconds = parseInt(body == null ? null : body.get("intervalSeconds"), 0);
        boolean canRetry = !"false".equalsIgnoreCase(body == null ? null : body.get("canRetry"));
        String failReasonCode = body == null ? null : body.get("failReasonCode");
        RetryPolicy policy = new RetryPolicy(maxAttempts, intervalSeconds, canRetry,
                failReasonCode == null || failReasonCode.isBlank() ? "RETRY_FAILED" : failReasonCode);
        QuarantineReprocessService.ReprocessResult result = reprocessService.retry(rawId, policy);
        if (!result.success) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(fail(result.code, result.reason));
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("decision", result.decision);
        data.put("reason", result.reason);
        data.put("summary", result.summary);
        return ResponseEntity.ok(ok(data));
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

    private int parseInt(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
