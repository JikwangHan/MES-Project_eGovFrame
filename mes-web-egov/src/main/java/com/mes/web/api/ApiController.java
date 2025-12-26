package com.mes.web.api;

import java.io.IOException;
import java.io.InputStream;
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

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class ApiController {
    // 목적: 샘플 JSON을 객체로 읽어오는 데 사용할 변환기이다.
    // 이유: DB 연결 전 단계에서는 파일 기반 샘플로 화면 바인딩을 검증해야 하므로 JSON 파싱이 필요하다.
    private final ObjectMapper mapper = new ObjectMapper();

    // 업링크 수신 엔드포인트.
    // 목적: 게이트웨이에서 올라온 원본 데이터를 먼저 수신한다.
    // 이유: 이후 정규화/검증/저장 로직을 추가하더라도 계약(엔드포인트)은 변하지 않아야 한다.
    // 입력: 요청 본문 JSON(현재는 그대로 반환).
    // 출력: 201 Created + 공통 응답 포맷.
    @PostMapping("/api/uplink")
    public ResponseEntity<Map<String, Object>> uplink(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ok(body));
    }

    // 제조장비가 MES로 직접 연동할 때 사용하는 엔드포인트.
    // 목적: 게이트웨이 경유와 직접 연동을 구분한다.
    // 이유: 프로토콜 정의서가 확정되면 장비 직결 로직을 독립적으로 발전시킬 수 있다.
    // 입력: 요청 본문 JSON(현재는 그대로 반환).
    // 출력: 201 Created + 공통 응답 포맷.
    @PostMapping("/api/direct-uplink")
    public ResponseEntity<Map<String, Object>> directUplink(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ok(body));
    }

    // 장비 목록 조회.
    // 목적: 설비현황 화면의 기본 목록을 제공한다.
    // 이유: UI 바인딩 단계에서는 실제 DB 대신 샘플 데이터가 필요하다.
    // 입력: status(선택), limit(기본 20).
    // 출력: 공통 응답 포맷 + 장비 리스트.
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

        // 샘플 파일이 있으면 그 값을 우선 사용한다.
        List<Map<String, Object>> list = sampleList("samples/equipments.json");
        if (list == null || list.isEmpty()) {
            list = new ArrayList<>();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("deviceId", "EQ-001");
            item.put("lastSeenAt", "2025-12-24T00:00:00");
            item.put("status", "OK");
            list.add(item);
        }
        // 필터가 들어오면 샘플 상태도 같이 맞춰서 보여준다.
        if (status != null) {
            for (Map<String, Object> item : list) {
                item.put("status", status.toUpperCase());
            }
        }
        list = limitList(list, limit);

        return ResponseEntity.ok(ok(list));
    }

    // 장비별 telemetry 조회.
    // 목적: 설비 상세 화면이 사용할 최소 데이터 흐름을 제공한다.
    // 이유: 실제 수집 모듈이 준비되기 전까지 샘플 데이터로 화면을 검증해야 한다.
    // 입력: deviceId(경로), limit(기본 20).
    // 출력: 공통 응답 포맷 + 장비별 telemetry 목록.
    @GetMapping("/api/equipments/{deviceId}/telemetry")
    public ResponseEntity<Map<String, Object>> telemetry(
            @PathVariable("deviceId") String deviceId,
            @RequestParam(name = "limit", required = false, defaultValue = "20") int limit) {
        if (!isValidLimit(limit)) {
            return badRequest("E-0001", "limit out of range");
        }

        // 샘플 파일이 없으면 기본 1건만 반환한다.
        List<Map<String, Object>> list = sampleList("samples/equipment-telemetry.json");
        if (list == null || list.isEmpty()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("deviceId", deviceId);
            item.put("timestamp", "2025-12-24T00:00:00");
            item.put("status", "OK");

            list = new ArrayList<>();
            list.add(item);
        } else {
            // 샘플이 있어도 deviceId는 요청 값으로 덮어쓴다(화면 일관성).
            for (Map<String, Object> item : list) {
                item.put("deviceId", deviceId);
            }
        }
        list = limitList(list, limit);

        return ResponseEntity.ok(ok(list));
    }

    // 대시보드 요약 정보.
    // 목적: 대시보드 카드에 표시할 최소 지표를 제공한다.
    // 이유: 실제 집계 로직이 없더라도 화면/스모크는 성공해야 한다.
    // 출력: okCount/warningCount/neverCount.
    @GetMapping("/api/dashboard/summary")
    public ResponseEntity<Map<String, Object>> summary() {
        Map<String, Object> data = sampleMap("samples/dashboard-summary.json");
        if (data == null || data.isEmpty()) {
            data = new LinkedHashMap<>();
            data.put("okCount", 1);
            data.put("warningCount", 0);
            data.put("neverCount", 0);
        }

        return ResponseEntity.ok(ok(data));
    }

    // 수주 목록 조회.
    // 목적: 수주 화면에 필요한 기본 리스트를 제공한다.
    // 이유: 계약 확정 전에도 화면을 연결해야 하므로 샘플 데이터를 사용한다.
    // 입력: limit(기본 20).
    // 출력: 공통 응답 포맷 + 수주 리스트.
    @GetMapping("/api/orders")
    public ResponseEntity<Map<String, Object>> orders(
            @RequestParam(name = "orderId", required = false) String orderId,
            @RequestParam(name = "partnerName", required = false) String partnerName,
            @RequestParam(name = "dueFrom", required = false) String dueFrom,
            @RequestParam(name = "dueTo", required = false) String dueTo,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "limit", required = false, defaultValue = "20") int limit) {
        if (!isValidLimit(limit)) {
            return badRequest("E-0001", "limit out of range");
        }
        if (!isValidDate(dueFrom) || !isValidDate(dueTo)) {
            return badRequest("E-1001", "invalid date format");
        }
        if (!isValidDateRange(dueFrom, dueTo)) {
            return badRequest("E-1003", "invalid date range");
        }
        if (!isValidStatusCode(status)) {
            return badRequest("E-1002", "invalid status");
        }

        // 샘플 파일이 없으면 1건 기본값을 만든다.
        List<Map<String, Object>> list = sampleList("samples/orders.json");
        if (list == null || list.isEmpty()) {
            list = new ArrayList<>();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("orderId", "ORD-001");
            item.put("productCode", "P-100");
            item.put("productName", "SampleProduct");
            item.put("quantity", 120);
            item.put("dueDate", "2025-12-31");
            item.put("status", "PLANNED");
            list.add(item);
        }
        // 필터 조건이 있으면 샘플 데이터에서도 동일하게 적용한다.
        list = filterOrders(list, orderId, partnerName, dueFrom, dueTo, status);
        list = limitList(list, limit);

        return ResponseEntity.ok(ok(list));
    }

    // 작업 목록 조회.
    // 목적: 작업 화면에 필요한 기본 리스트를 제공한다.
    // 이유: 수주와 같은 방식으로 샘플 데이터로 먼저 UI를 연결한다.
    // 입력: limit(기본 20).
    // 출력: 공통 응답 포맷 + 작업 리스트.
    @GetMapping("/api/jobs")
    public ResponseEntity<Map<String, Object>> jobs(
            @RequestParam(name = "jobId", required = false) String jobId,
            @RequestParam(name = "orderId", required = false) String orderId,
            @RequestParam(name = "processName", required = false) String processName,
            @RequestParam(name = "from", required = false) String from,
            @RequestParam(name = "to", required = false) String to,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "limit", required = false, defaultValue = "20") int limit) {
        if (!isValidLimit(limit)) {
            return badRequest("E-0001", "limit out of range");
        }
        if (!isValidDate(from) || !isValidDate(to)) {
            return badRequest("E-1001", "invalid date format");
        }
        if (!isValidDateRange(from, to)) {
            return badRequest("E-1003", "invalid date range");
        }
        if (!isValidStatusCode(status)) {
            return badRequest("E-1002", "invalid status");
        }

        // 샘플 파일이 없으면 1건 기본값을 만든다.
        List<Map<String, Object>> list = sampleList("samples/jobs.json");
        if (list == null || list.isEmpty()) {
            list = new ArrayList<>();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("jobId", "JOB-001");
            item.put("orderId", "ORD-001");
            item.put("processName", "Cutting");
            item.put("startAt", "2025-12-24T09:00:00");
            item.put("endAt", "2025-12-24T12:00:00");
            item.put("status", "DONE");
            list.add(item);
        }
        // 필터 조건이 있으면 샘플 데이터에서도 동일하게 적용한다.
        list = filterJobs(list, jobId, orderId, processName, from, to, status);
        list = limitList(list, limit);

        return ResponseEntity.ok(ok(list));
    }

    // KPI 목록 조회.
    // 목적: KPI 화면에서 그리드/차트에 사용할 기초 데이터를 제공한다.
    // 이유: KPI 화면 바인딩을 먼저 진행하기 위해 샘플 데이터가 필요하다.
    // 입력: limit(기본 20).
    // 출력: 공통 응답 포맷 + KPI 리스트.
    @GetMapping("/api/kpi")
    public ResponseEntity<Map<String, Object>> kpi(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "kpiId", required = false) String kpiId,
            @RequestParam(name = "from", required = false) String from,
            @RequestParam(name = "to", required = false) String to,
            @RequestParam(name = "limit", required = false, defaultValue = "20") int limit) {
        if (!isValidLimit(limit)) {
            return badRequest("E-0001", "limit out of range");
        }
        if (!isValidDate(from) || !isValidDate(to)) {
            return badRequest("E-1001", "invalid date format");
        }
        if (!isValidDateRange(from, to)) {
            return badRequest("E-1003", "invalid date range");
        }

        List<Map<String, Object>> list = sampleList("samples/kpi.json");
        if (list == null || list.isEmpty()) {
            list = new ArrayList<>();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("kpiId", "KPI-001");
            item.put("name", "생산성");
            item.put("targetValue", 100);
            item.put("currentValue", 82);
            item.put("progressRate", 0.82);
            item.put("resultValue", 82);
            item.put("unit", "%");
            item.put("formula", "current/target*100");
            item.put("remark", "샘플");
            item.put("date", "2025-12-24");
            list.add(item);
        }
        // 필터 조건이 있으면 샘플 데이터에서도 동일하게 적용한다.
        list = filterKpi(list, name, kpiId, from, to);
        list = limitList(list, limit);

        return ResponseEntity.ok(ok(list));
    }

    // KPI 추이 조회.
    // 목적: KPI 차트에서 기간별 추이를 보여주기 위한 데이터를 제공한다.
    // 이유: 차트 바인딩 검증은 별도의 시계열 데이터가 필요하기 때문이다.
    // 입력: limit(기본 20).
    // 출력: 공통 응답 포맷 + KPI 추이 리스트.
    @GetMapping("/api/kpi/trend")
    public ResponseEntity<Map<String, Object>> kpiTrend(
            @RequestParam(name = "kpiId", required = false) String kpiId,
            @RequestParam(name = "from", required = false) String from,
            @RequestParam(name = "to", required = false) String to,
            @RequestParam(name = "limit", required = false, defaultValue = "20") int limit) {
        if (!isValidLimit(limit)) {
            return badRequest("E-0001", "limit out of range");
        }
        if (!isValidDate(from) || !isValidDate(to)) {
            return badRequest("E-1001", "invalid date format");
        }
        if (!isValidDateRange(from, to)) {
            return badRequest("E-1003", "invalid date range");
        }

        List<Map<String, Object>> list = sampleList("samples/kpi-trend.json");
        if (list == null || list.isEmpty()) {
            list = new ArrayList<>();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("kpiId", "KPI-001");
            item.put("name", "생산성");
            item.put("date", "2025-12-24");
            item.put("targetValue", 100);
            item.put("currentValue", 82);
            list.add(item);
        }
        // 필터 조건이 있으면 샘플 데이터에서도 동일하게 적용한다.
        list = filterKpiTrend(list, kpiId, from, to);
        list = limitList(list, limit);

        return ResponseEntity.ok(ok(list));
    }

    // 공통 성공 응답 포맷을 만든다.
    // 목적: 모든 API 응답을 동일한 구조로 통일한다.
    // 이유: 프론트/연동 모듈이 항상 같은 포맷을 기대하기 때문이다.
    // 입력: 실제 데이터(또는 목록).
    // 출력: result/message/data 형태의 Map.
    private Map<String, Object> ok(Object data) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("result", "OK");
        res.put("message", "");
        res.put("data", data);
        return res;
    }

    // 공통 실패 응답 포맷을 만든다.
    // 목적: 오류 상황에서 일관된 에러 구조를 제공한다.
    // 이유: 스모크/운영 로그에서 "무엇이 잘못됐는지"를 한 줄로 판단할 수 있게 한다.
    // 입력: errorCode, message.
    // 출력: 400 응답 + 공통 에러 포맷.
    private ResponseEntity<Map<String, Object>> badRequest(String errorCode, String message) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("result", "FAIL");
        res.put("message", message);
        res.put("errorCode", errorCode);
        res.put("data", null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    // limit의 허용 범위를 검증한다.
    // 목적: 한 번에 너무 많은 데이터가 조회되는 것을 방지한다.
    // 이유: 과도한 조회는 성능/응답 시간을 악화시킨다.
    private boolean isValidLimit(int limit) {
        return limit >= 1 && limit <= 100;
    }

    // status의 허용 값만 통과시킨다.
    // 목적: 정해진 상태 값만 조회 조건으로 허용한다.
    // 이유: 잘못된 값이 들어오면 화면/통계가 깨질 수 있다.
    private boolean isValidStatus(String status) {
        String upper = status.toUpperCase();
        return "OK".equals(upper) || "WARNING".equals(upper) || "NEVER".equals(upper);
    }

    // 날짜 형식(YYYY-MM-DD)만 허용한다.
    // 이유: 검색 조건이 일관된 형식을 갖추어야 비교가 가능하다.
    private boolean isValidDate(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        if (!value.matches("\\\\d{4}-\\\\d{2}-\\\\d{2}")) {
            return false;
        }
        String[] parts = value.split("-");
        if (parts.length != 3) {
            return false;
        }
        int year = parseInt(parts[0]);
        int month = parseInt(parts[1]);
        int day = parseInt(parts[2]);
        if (month < 1 || month > 12) {
            return false;
        }
        int[] days = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
        boolean leap = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
        if (leap) {
            days[1] = 29;
        }
        return day >= 1 && day <= days[month - 1];
    }

    // 문자열을 안전하게 정수로 변환한다.
    // 이유: 잘못된 숫자가 들어와도 예외 대신 0으로 처리해 검증 로직을 유지한다.
    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    // 상태 코드(PLANNED/IN_PROGRESS/DONE)만 허용한다.
    // 이유: 검색 조건에서 정의된 값만 처리하기 위함이다.
    private boolean isValidStatusCode(String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        String upper = status.toUpperCase();
        return "PLANNED".equals(upper) || "IN_PROGRESS".equals(upper) || "DONE".equals(upper);
    }

    // 기간 역전(from > to)인지 확인한다.
    // 이유: 시작일이 종료일보다 늦으면 의미 없는 검색이기 때문이다.
    private boolean isValidDateRange(String from, String to) {
        if (from == null || from.isBlank() || to == null || to.isBlank()) {
            return true;
        }
        return from.compareTo(to) <= 0;
    }

    // 샘플 JSON 파일을 읽어 Object로 변환한다.
    // 목적: 리소스 폴더에 있는 샘플 JSON을 읽는다.
    // 이유: DB 연동 전 단계에서도 화면/스모크를 빠르게 검증할 수 있다.
    // 입력: 클래스패스 기준 경로.
    // 출력: JSON을 Object로 변환한 결과(없으면 null).
    private Object readSample(String path) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(path)) {
            if (input == null) {
                return null;
            }
            return mapper.readValue(input, Object.class);
        } catch (IOException ex) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> sampleList(String path) {
        // JSON이 배열인 경우를 리스트로 반환한다.
        Object data = readSample(path);
        if (data instanceof List) {
            return (List<Map<String, Object>>) data;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> sampleMap(String path) {
        // JSON이 객체인 경우를 맵으로 반환한다.
        Object data = readSample(path);
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        }
        return null;
    }

    private List<Map<String, Object>> limitList(List<Map<String, Object>> list, int limit) {
        // limit보다 많은 경우 앞에서부터 잘라서 반환한다.
        if (list.size() <= limit) {
            return list;
        }
        return new ArrayList<>(list.subList(0, limit));
    }

    // 공통 문자열 변환.
    // 이유: Map에 들어있는 값 타입이 일정하지 않을 수 있어 안전하게 문자열로 변환한다.
    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    // 부분 일치(대소문자 무시) 조건을 확인한다.
    // 이유: 검색 UI가 "부분 일치"를 기본으로 하기 때문이다.
    private boolean matchContains(Object value, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String text = asString(value).toLowerCase();
        String q = query.toLowerCase();
        return text.contains(q);
    }

    // 완전 일치(대소문자 무시) 조건을 확인한다.
    // 이유: 상태값 같은 고정 코드는 정확한 일치가 필요하다.
    private boolean matchEquals(Object value, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String text = asString(value).toLowerCase();
        String q = query.toLowerCase();
        return text.equals(q);
    }

    // 날짜 범위 조건을 확인한다.
    // 이유: 기간 검색이 많은 MES 화면 특성을 반영한다.
    private boolean matchDateRange(Object value, String from, String to) {
        if ((from == null || from.isBlank()) && (to == null || to.isBlank())) {
            return true;
        }
        String date = normalizeDate(asString(value));
        if (date.isBlank()) {
            return false;
        }
        if (from != null && !from.isBlank() && date.compareTo(from) < 0) {
            return false;
        }
        if (to != null && !to.isBlank() && date.compareTo(to) > 0) {
            return false;
        }
        return true;
    }

    // 날짜 문자열에서 YYYY-MM-DD만 추출한다.
    // 이유: 시간이 포함된 값(예: 2025-12-24T09:00:00)도 동일하게 비교하기 위함이다.
    private String normalizeDate(String value) {
        if (value == null) {
            return "";
        }
        return value.length() >= 10 ? value.substring(0, 10) : value;
    }

    // 수주 필터를 적용한다.
    // 이유: 화면 필터와 API 파라미터가 연결된 것처럼 동작해야 한다.
    private List<Map<String, Object>> filterOrders(List<Map<String, Object>> list,
            String orderId, String partnerName, String dueFrom, String dueTo, String status) {
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> item : list) {
            if (!matchContains(item.get("orderId"), orderId)) {
                continue;
            }
            if (!matchContains(item.get("partnerName"), partnerName)) {
                continue;
            }
            if (!matchDateRange(item.get("dueDate"), dueFrom, dueTo)) {
                continue;
            }
            if (!matchEquals(item.get("status"), status)) {
                continue;
            }
            filtered.add(item);
        }
        return filtered;
    }

    // 작업 필터를 적용한다.
    // 이유: 작업 화면의 검색 조건이 API에 반영되는지 확인하기 위함이다.
    private List<Map<String, Object>> filterJobs(List<Map<String, Object>> list,
            String jobId, String orderId, String processName, String from, String to, String status) {
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> item : list) {
            if (!matchContains(item.get("jobId"), jobId)) {
                continue;
            }
            if (!matchContains(item.get("orderId"), orderId)) {
                continue;
            }
            if (!matchContains(item.get("processName"), processName)) {
                continue;
            }
            if (!matchDateRange(item.get("startAt"), from, to)) {
                continue;
            }
            if (!matchEquals(item.get("status"), status)) {
                continue;
            }
            filtered.add(item);
        }
        return filtered;
    }

    // KPI 필터를 적용한다.
    // 이유: KPI 화면에서 입력한 조건이 결과에 반영되어야 한다.
    private List<Map<String, Object>> filterKpi(List<Map<String, Object>> list,
            String name, String kpiId, String from, String to) {
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> item : list) {
            if (!matchContains(item.get("name"), name)) {
                continue;
            }
            if (!matchEquals(item.get("kpiId"), kpiId)) {
                continue;
            }
            if (!matchDateRange(item.get("date"), from, to)) {
                continue;
            }
            filtered.add(item);
        }
        return filtered;
    }

    // KPI 추이 필터를 적용한다.
    // 이유: 기간/ID 조건에 맞는 추이만 차트에 표시하기 위함이다.
    private List<Map<String, Object>> filterKpiTrend(List<Map<String, Object>> list,
            String kpiId, String from, String to) {
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> item : list) {
            if (!matchEquals(item.get("kpiId"), kpiId)) {
                continue;
            }
            if (!matchDateRange(item.get("date"), from, to)) {
                continue;
            }
            filtered.add(item);
        }
        return filtered;
    }
}
