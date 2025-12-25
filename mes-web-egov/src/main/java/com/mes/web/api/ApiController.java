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
    // 업링크 수신 엔드포인트.
    // 이유: 게이트웨이에서 올라온 원본 데이터를 우선 받아두고, 이후 정규화/검증 단계로 확장하기 위해 최소 응답을 유지한다.
    @PostMapping("/api/uplink")
    public ResponseEntity<Map<String, Object>> uplink(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ok(body));
    }

    // 제조장비가 MES로 직접 연동할 때 사용하는 엔드포인트.
    // 이유: 게이트웨이 경유와 직접 연동을 분리하여 추후 프로토콜 정의서에 따라 처리 로직을 분리하기 쉽다.
    @PostMapping("/api/direct-uplink")
    public ResponseEntity<Map<String, Object>> directUplink(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ok(body));
    }

    // 장비 목록 조회.
    // 이유: UI/대시보드 최소 기능을 만족하기 위해 status/limit 가드만 먼저 둔다.
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

        // 아직 DB 연동 전이므로 예시 데이터를 반환한다.
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("deviceId", "EQ-001");
        item.put("lastSeenAt", "2025-12-24T00:00:00");
        item.put("status", status == null ? "OK" : status.toUpperCase());
        list.add(item);

        return ResponseEntity.ok(ok(list));
    }

    // 장비별 telemetry 조회.
    // 이유: 장비 상세 화면의 최소 데이터 흐름을 보장하기 위해 단순 응답 형태로 시작한다.
    @GetMapping("/api/equipments/{deviceId}/telemetry")
    public ResponseEntity<Map<String, Object>> telemetry(
            @PathVariable("deviceId") String deviceId,
            @RequestParam(name = "limit", required = false, defaultValue = "20") int limit) {
        if (!isValidLimit(limit)) {
            return badRequest("E-0001", "limit out of range");
        }
        // 아직 실시간 수집 전이므로 예시 telemetry만 제공한다.
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("deviceId", deviceId);
        item.put("timestamp", "2025-12-24T00:00:00");
        item.put("status", "OK");

        List<Map<String, Object>> list = new ArrayList<>();
        list.add(item);

        return ResponseEntity.ok(ok(list));
    }

    // 대시보드 요약 정보.
    // 이유: UI 요구사항의 최소 대시보드 지표를 먼저 고정하기 위해 간단한 카운트만 반환한다.
    @GetMapping("/api/dashboard/summary")
    public ResponseEntity<Map<String, Object>> summary() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("okCount", 1);
        data.put("warningCount", 0);
        data.put("neverCount", 0);

        return ResponseEntity.ok(ok(data));
    }

    // 공통 성공 응답 포맷을 만든다.
    // 이유: 프론트/연동이 동일 포맷을 기대하므로, 응답 형태를 일관되게 유지한다.
    private Map<String, Object> ok(Object data) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("result", "OK");
        res.put("message", "");
        res.put("data", data);
        return res;
    }

    // 공통 실패 응답 포맷을 만든다.
    // 이유: 오류 코드와 메시지를 명시하여 스모크/운영 로그에서 판단 가능하게 한다.
    private ResponseEntity<Map<String, Object>> badRequest(String errorCode, String message) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("result", "FAIL");
        res.put("message", message);
        res.put("errorCode", errorCode);
        res.put("data", null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    // limit의 허용 범위를 검증한다.
    // 이유: 과도한 조회로 인한 성능 저하를 사전에 방지한다.
    private boolean isValidLimit(int limit) {
        return limit >= 1 && limit <= 100;
    }

    // status의 허용 값만 통과시킨다.
    // 이유: UI/조회 로직에서 정의한 상태 값 외에는 거부해 데이터 정합성을 유지한다.
    private boolean isValidStatus(String status) {
        String upper = status.toUpperCase();
        return "OK".equals(upper) || "WARNING".equals(upper) || "NEVER".equals(upper);
    }
}
