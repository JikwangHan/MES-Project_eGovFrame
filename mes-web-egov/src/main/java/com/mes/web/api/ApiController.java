package com.mes.web.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {
    @PostMapping("/api/uplink")
    public ResponseEntity<Map<String, Object>> uplink(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ok(body));
    }

    @PostMapping("/api/direct-uplink")
    public ResponseEntity<Map<String, Object>> directUplink(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ok(body));
    }

    @GetMapping("/api/equipments")
    public ResponseEntity<Map<String, Object>> equipments(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "limit", required = false, defaultValue = "20") int limit) {
        if (!isValidLimit(limit)) {
            return badRequest("E-0001", "limit out of range");
        }
        if (status != null && !isValidStatus(status)) {
            return badRequest("E-0001", "invalid status");
        }

        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("deviceId", "EQ-001");
        item.put("lastSeenAt", "2025-12-24T00:00:00");
        item.put("status", status == null ? "OK" : status.toUpperCase());
        list.add(item);

        return ResponseEntity.ok(ok(list));
    }

    @GetMapping("/api/equipments/{deviceId}/telemetry")
    public ResponseEntity<Map<String, Object>> telemetry(
            @PathVariable("deviceId") String deviceId,
            @RequestParam(name = "limit", required = false, defaultValue = "20") int limit) {
        if (!isValidLimit(limit)) {
            return badRequest("E-0001", "limit out of range");
        }
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("deviceId", deviceId);
        item.put("timestamp", "2025-12-24T00:00:00");
        item.put("status", "OK");

        List<Map<String, Object>> list = new ArrayList<>();
        list.add(item);

        return ResponseEntity.ok(ok(list));
    }

    @GetMapping("/api/dashboard/summary")
    public ResponseEntity<Map<String, Object>> summary() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("okCount", 1);
        data.put("warningCount", 0);
        data.put("neverCount", 0);

        return ResponseEntity.ok(ok(data));
    }

    private Map<String, Object> ok(Object data) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("result", "OK");
        res.put("message", "");
        res.put("data", data);
        return res;
    }

    private ResponseEntity<Map<String, Object>> badRequest(String errorCode, String message) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("result", "FAIL");
        res.put("message", message);
        res.put("errorCode", errorCode);
        res.put("data", null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    private boolean isValidLimit(int limit) {
        return limit >= 1 && limit <= 100;
    }

    private boolean isValidStatus(String status) {
        String upper = status.toUpperCase();
        return "OK".equals(upper) || "WARNING".equals(upper) || "NEVER".equals(upper);
    }
}
