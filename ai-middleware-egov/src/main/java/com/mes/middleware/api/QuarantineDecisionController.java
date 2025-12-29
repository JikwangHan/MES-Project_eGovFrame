package com.mes.middleware.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mes.middleware.pipeline.QuarantineDecisionService;

@RestController
public class QuarantineDecisionController {
    // 목적: 승인/거부 처리 요청을 받아 흐름을 연결한다.
    // 이유: 운영자가 API로 승인/거부를 통제할 수 있어야 하기 때문이다.
    private final QuarantineDecisionService decisionService;

    public QuarantineDecisionController(QuarantineDecisionService decisionService) {
        this.decisionService = decisionService;
    }

    // 목적: 승인/거부 처리 요청을 수행한다.
    // 이유: 재처리 결과를 확정해야 통계 집계가 가능해진다.
    @PostMapping("/api/quarantine/decision")
    public ResponseEntity<Map<String, Object>> decide(@RequestBody Map<String, String> body) {
        String rawId = body == null ? null : body.get("rawId");
        String decision = body == null ? null : body.get("decision");
        String summary = body == null ? null : body.get("summary");
        QuarantineDecisionService.DecisionResult result = decisionService.decide(rawId, decision, summary);
        if (!result.success) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(fail(result.code, result.summary));
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("decision", result.decision);
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
}
