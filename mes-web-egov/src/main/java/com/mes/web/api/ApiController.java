package com.mes.web.api;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mes.web.db.DbSupport;

@RestController
public class ApiController {
    // 이 컨트롤러는 "MES Web 서비스의 API 계약"을 실제로 노출하는 역할을 한다.
    // 설계상 아직 DB가 완전히 준비되지 않았을 수 있으므로,
    // 1) DB 접속 정보가 있으면 실제 DB를 사용하고,
    // 2) 없으면 메모리 저장소(샘플 데이터)를 사용한다.
    // 이렇게 하면 개발 초반부터 화면/스모크 테스트가 끊기지 않는다.
    //
    // 초보자 설명:
    // - "API 계약"이란, 어떤 주소로 어떤 데이터를 주고받는지의 약속이다.
    // - 이 파일은 그 약속을 실제로 동작하게 만드는 곳이다.
    // - DB가 아직 준비되지 않아도 화면을 먼저 만들 수 있게 임시 저장소를 함께 둔다.
    // 목적: 공통 company_id 기본값을 고정한다.
    // 이유: 멀티테넌시 확장 전 단계에서는 최소 1개 회사 기준으로 CRUD를 유지해야 하기 때문이다.
    private static final long DEFAULT_COMPANY_ID = 1L;

    // 목적: DB 사용 가능 여부를 판단하기 위한 공통 지원 객체다.
    // 이유: 접속 정보가 없으면 메모리 저장소로 자동 전환되어야 하므로 분기 기준이 필요하다.
    private final DbSupport dbSupport;

    // 목적: Spring이 DbSupport를 주입하도록 생성자를 명시한다.
    // 이유: DB/메모리 분기 로직을 모든 엔드포인트에서 동일하게 사용하기 위해서다.
    public ApiController(DbSupport dbSupport) {
        this.dbSupport = dbSupport;
    }

    // 목적: 샘플 JSON을 객체로 읽어오는 데 사용할 변환기이다.
    // 이유: DB 연결 전 단계에서는 파일 기반 샘플로 화면 바인딩을 검증해야 하므로 JSON 파싱이 필요하다.
    private final ObjectMapper mapper = new ObjectMapper();

    // 목적: DB가 없는 상황에서도 CRUD 흐름을 검증할 수 있도록 메모리 저장소를 둔다.
    // 이유: 본개발 전 단계에서도 화면/계약 테스트를 통과해야 하므로 임시 저장소가 필요하다.
    private final List<Map<String, Object>> equipmentStore = Collections.synchronizedList(new ArrayList<>());
    private final List<Map<String, Object>> orderStore = Collections.synchronizedList(new ArrayList<>());
    private final List<Map<String, Object>> jobStore = Collections.synchronizedList(new ArrayList<>());
    private final List<Map<String, Object>> kpiStore = Collections.synchronizedList(new ArrayList<>());
    private final List<Map<String, Object>> externalSyncStore = Collections.synchronizedList(new ArrayList<>());

    // 목적: 임시 식별자 생성 규칙을 고정한다.
    // 이유: 자동 생성된 ID가 중복되지 않도록 보장해야 한다.
    private final AtomicInteger equipmentSeq = new AtomicInteger(2);
    private final AtomicInteger orderSeq = new AtomicInteger(2);
    private final AtomicInteger jobSeq = new AtomicInteger(2);
    private final AtomicInteger kpiSeq = new AtomicInteger(2);

    // ---------------------------------------------------------------------
    // 핵심 동작 규칙(초보자용 요약)
    // 1) DB 접속 정보가 있으면 DB에서 조회/등록/수정/삭제를 수행한다.
    // 2) DB 접속 정보가 없으면 메모리 저장소로 동일 기능을 흉내낸다.
    // 이렇게 하면 "계약과 테스트"가 먼저 통과되고, DB가 준비되면 자동으로 전환된다.
    // ---------------------------------------------------------------------

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

    // 외부기관 연계 트리거.
    // 목적: 외부기관으로 데이터를 전송하기 위한 요청을 수신한다.
    // 이유: 외부기관 API 스펙이 확정되기 전에도 연계 흐름을 검증해야 한다.
    // 입력: companyId/from/to (선택).
    // 출력: requestId (연계 요청 식별자).
    @PostMapping("/api/external-sync")
    public ResponseEntity<Map<String, Object>> externalSync(@RequestBody(required = false) String body) {
        // 초보자 설명:
        // - 외부기관 연계는 "기간(from~to) 동안의 데이터를 전송해 달라"는 요청이다.
        // - 아직 실제 전송이 없어도 요청 흐름을 먼저 만든다.
        Map<String, Object> payload = new LinkedHashMap<>();
        if (body != null && !body.isBlank()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> parsed = mapper.readValue(body, Map.class);
                if (parsed != null) {
                    payload.putAll(parsed);
                }
            } catch (IOException ex) {
                return badRequest("E-0001", "invalid request body");
            }
        }
        String from = normalizeDate(asString(payload.get("from")));
        String to = normalizeDate(asString(payload.get("to")));
        // 초보자 설명:
        // - from/to 둘 다 비어 있으면 "전체 기간"으로 간주하고 그대로 진행한다.
        // - 둘 중 하나라도 들어오면 날짜 형식과 기간 범위를 반드시 검증한다.
        boolean hasFrom = (from != null && !from.isBlank());
        boolean hasTo = (to != null && !to.isBlank());
        if (hasFrom ^ hasTo) {
            // 초보자 설명:
            // - from/to 중 하나만 들어오면 기간의 시작/끝이 불명확해진다.
            // - 그래서 둘 중 하나라도 들어오면 반드시 둘 다 제공하도록 강제한다.
            return badRequest("E-1001", "invalid date format");
        }
        if (hasFrom || hasTo) {
            if (!isValidDate(from) || !isValidDate(to)) {
                return badRequest("E-1001", "invalid date format");
            }
            if (!isValidDateRange(from, to)) {
                return badRequest("E-1003", "invalid date range");
            }
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("requestId", "SYNC-" + System.currentTimeMillis());
        data.put("status", "ACCEPTED");
        data.put("acceptedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return ResponseEntity.ok(ok(data));
    }

    // 외부기관 연계 이력 조회.
    // 목적: 연계 요청 이력을 화면 필터와 연결해 조회한다.
    // 이유: API 계약이 확정되기 전에도 조회 흐름을 검증해야 한다.
    // 입력: requestId/agency/from/to/status/keyword/limit(선택).
    // 출력: 공통 응답 포맷 + 이력 리스트.
    @GetMapping("/api/external-sync/logs")
    public ResponseEntity<Map<String, Object>> externalSyncLogs(
            @RequestParam(name = "requestId", required = false) String requestId,
            @RequestParam(name = "agency", required = false) String agency,
            @RequestParam(name = "from", required = false) String from,
            @RequestParam(name = "to", required = false) String to,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "limit", required = false, defaultValue = "20") int limit) {
        // 초보자 설명:
        // - 조회 조건이 잘못되면 서버가 먼저 알려줘야 화면이 안정적으로 동작한다.
        // - 날짜/상태/limit 검증을 여기서 처리한다.
        if (!isValidLimit(limit)) {
            return badRequest("E-0001", "limit out of range");
        }
        boolean hasFrom = (from != null && !from.isBlank());
        boolean hasTo = (to != null && !to.isBlank());
        if (hasFrom ^ hasTo) {
            return badRequest("E-1001", "invalid date format");
        }
        if (hasFrom || hasTo) {
            if (!isValidDate(from) || !isValidDate(to)) {
                return badRequest("E-1001", "invalid date format");
            }
            if (!isValidDateRange(from, to)) {
                return badRequest("E-1003", "invalid date range");
            }
        }
        if (status != null && !status.isBlank() && !isValidSyncStatus(status)) {
            return badRequest("E-1002", "invalid status");
        }

        List<Map<String, Object>> list = ensureExternalSyncStore();
        list = filterExternalSyncLogs(list, requestId, agency, from, to, status, keyword);
        list = limitList(list, limit);

        return ResponseEntity.ok(ok(list));
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
        // 흐름 요약:
        // 1) 파라미터 검증
        // 2) DB 사용 가능하면 DB 조회
        // 3) DB가 없으면 메모리/샘플 데이터로 응답
        if (!isValidLimit(limit)) {
            return badRequest("E-0001", "limit out of range");
        }
        if (status != null && !isValidStatus(status)) {
            return badRequest("E-0001", "invalid status");
        }

        if (isDbEnabled()) {
            List<Object> params = new ArrayList<>();
            StringBuilder sql = new StringBuilder(
                    "SELECT id, device_code, name, model, vendor, status, updated_at FROM equipment WHERE company_id = ?");
            params.add(DEFAULT_COMPANY_ID);
            if (status != null) {
                sql.append(" AND status = ?");
                params.add(status.toUpperCase());
            }
            sql.append(" ORDER BY id DESC LIMIT ?");
            params.add(limit);

            // DB 쿼리 문자열은 null이 될 수 없도록 안전 변환을 거친다.
            List<Map<String, Object>> rows = jdbc().queryForList(requireSql(sql), params.toArray(new Object[0]));
            List<Map<String, Object>> list = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                list.add(mapEquipmentRow(row));
            }
            return ResponseEntity.ok(ok(list));
        }

        // 샘플 파일이 있으면 그 값을 우선 사용한다.
        List<Map<String, Object>> list = ensureEquipmentStore();
        // 필터가 들어오면 샘플 상태도 같이 맞춰서 보여준다.
        if (status != null) {
            for (Map<String, Object> item : list) {
                item.put("status", status.toUpperCase());
            }
        }
        list = limitList(list, limit);

        return ResponseEntity.ok(ok(list));
    }

    // 장비 단건 조회.
    // 목적: 상세 화면에서 특정 장비의 기본 정보를 조회한다.
    // 이유: 목록뿐 아니라 상세 화면 연결도 동시에 검증해야 한다.
    @GetMapping("/api/equipments/{deviceId}")
    public ResponseEntity<Map<String, Object>> equipmentDetail(@PathVariable("deviceId") String deviceId) {
        if (isDbEnabled()) {
            Map<String, Object> item = dbFindEquipment(deviceId);
            if (item == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "equipment not found"));
            }
            return ResponseEntity.ok(ok(item));
        }

        Map<String, Object> item = findByKey(ensureEquipmentStore(), "deviceId", deviceId);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "equipment not found"));
        }
        return ResponseEntity.ok(ok(item));
    }

    // 장비 등록.
    // 목적: 장비 기본 CRUD 흐름을 검증한다.
    // 입력: deviceId(선택), name(필수), status(선택).
    @PostMapping("/api/equipments")
    public ResponseEntity<Map<String, Object>> createEquipment(@RequestBody Map<String, Object> body) {
        // 장비 등록 처리 흐름:
        // - 필수값(name) 확인
        // - deviceId가 없으면 자동 생성
        // - DB 가능 여부에 따라 DB 또는 메모리 저장소 사용
        String name = asString(body.get("name"));
        if (name.isBlank()) {
            return badRequest("E-0001", "name required");
        }
        String deviceId = asString(body.get("deviceId"));
        if (deviceId.isBlank()) {
            deviceId = "EQ-" + String.format("%03d", equipmentSeq.getAndIncrement());
        }
        if (isDbEnabled()) {
            String model = asString(body.get("model"));
            String vendor = asString(body.get("vendor"));
            String status = defaultStatus(asString(body.get("status")), "ACTIVE");
            jdbc().update(
                    "INSERT INTO equipment (company_id, device_code, name, model, vendor, status) VALUES (?, ?, ?, ?, ?, ?)",
                    DEFAULT_COMPANY_ID, deviceId, name, model, vendor, status);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("deviceId", deviceId);
            item.put("name", name);
            item.put("model", model);
            item.put("vendor", vendor);
            item.put("lastSeenAt", "");
            item.put("status", status);
            return ResponseEntity.status(HttpStatus.CREATED).body(ok(item));
        }

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("deviceId", deviceId);
        item.put("name", name);
        item.put("model", asString(body.get("model")));
        item.put("vendor", asString(body.get("vendor")));
        item.put("lastSeenAt", asString(body.get("lastSeenAt")));
        item.put("status", defaultStatus(asString(body.get("status")), "ACTIVE"));
        equipmentStore.add(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(ok(item));
    }

    // 장비 수정.
    // 목적: 수정 흐름을 검증한다.
    @PutMapping("/api/equipments/{deviceId}")
    public ResponseEntity<Map<String, Object>> updateEquipment(
            @PathVariable("deviceId") String deviceId,
            @RequestBody Map<String, Object> body) {
        if (isDbEnabled()) {
            Map<String, Object> existing = dbFindEquipment(deviceId);
            if (existing == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "equipment not found"));
            }
            applyIfPresent(existing, "name", body);
            applyIfPresent(existing, "model", body);
            applyIfPresent(existing, "vendor", body);
            if (body.containsKey("status")) {
                existing.put("status", defaultStatus(asString(body.get("status")), "ACTIVE"));
            }
            jdbc().update(
                    "UPDATE equipment SET name = ?, model = ?, vendor = ?, status = ?, updated_at = NOW() WHERE company_id = ? AND device_code = ?",
                    asString(existing.get("name")),
                    asString(existing.get("model")),
                    asString(existing.get("vendor")),
                    asString(existing.get("status")),
                    DEFAULT_COMPANY_ID,
                    deviceId);
            return ResponseEntity.ok(ok(existing));
        }

        Map<String, Object> item = findByKey(ensureEquipmentStore(), "deviceId", deviceId);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "equipment not found"));
        }
        applyIfPresent(item, "name", body);
        applyIfPresent(item, "model", body);
        applyIfPresent(item, "vendor", body);
        applyIfPresent(item, "lastSeenAt", body);
        if (body.containsKey("status")) {
            item.put("status", defaultStatus(asString(body.get("status")), "ACTIVE"));
        }
        return ResponseEntity.ok(ok(item));
    }

    // 장비 삭제.
    // 목적: 삭제 흐름을 검증한다.
    @DeleteMapping("/api/equipments/{deviceId}")
    public ResponseEntity<Map<String, Object>> deleteEquipment(@PathVariable("deviceId") String deviceId) {
        if (isDbEnabled()) {
            int updated = jdbc().update(
                    "DELETE FROM equipment WHERE company_id = ? AND device_code = ?",
                    DEFAULT_COMPANY_ID, deviceId);
            if (updated == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "equipment not found"));
            }
            return ResponseEntity.ok(ok(Map.of("deleted", true)));
        }

        boolean removed = removeByKey(ensureEquipmentStore(), "deviceId", deviceId);
        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "equipment not found"));
        }
        return ResponseEntity.ok(ok(Map.of("deleted", true)));
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
        // 흐름 요약:
        // 1) 조회 조건 검증(날짜/상태)
        // 2) DB 가능 시 조건에 맞춰 SQL 생성
        // 3) DB 미사용 시 샘플/메모리에서 필터링
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

        if (isDbEnabled()) {
            List<Object> params = new ArrayList<>();
            StringBuilder sql = new StringBuilder(
                    "SELECT id, order_no, partner_name, due_date, status, product_code, product_name, quantity FROM orders WHERE company_id = ?");
            params.add(DEFAULT_COMPANY_ID);

            if (orderId != null && !orderId.isBlank()) {
                Long numeric = parseNumericId(orderId, "ORD-");
                if (numeric != null) {
                    sql.append(" AND id = ?");
                    params.add(numeric);
                } else {
                    sql.append(" AND order_no LIKE ?");
                    params.add("%" + orderId + "%");
                }
            }
            if (partnerName != null && !partnerName.isBlank()) {
                sql.append(" AND partner_name LIKE ?");
                params.add("%" + partnerName + "%");
            }
            if (dueFrom != null && !dueFrom.isBlank()) {
                sql.append(" AND due_date >= ?");
                params.add(dueFrom);
            }
            if (dueTo != null && !dueTo.isBlank()) {
                sql.append(" AND due_date <= ?");
                params.add(dueTo);
            }
            if (status != null && !status.isBlank()) {
                sql.append(" AND status = ?");
                params.add(status.toUpperCase());
            }

            sql.append(" ORDER BY id DESC LIMIT ?");
            params.add(limit);

            // DB 쿼리 문자열은 null이 될 수 없도록 안전 변환을 거친다.
            List<Map<String, Object>> rows = jdbc().queryForList(requireSql(sql), params.toArray(new Object[0]));
            List<Map<String, Object>> list = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                list.add(mapOrderRow(row));
            }
            return ResponseEntity.ok(ok(list));
        }

        // 샘플 파일이 없으면 1건 기본값을 만든다.
        List<Map<String, Object>> list = ensureOrderStore();
        // 필터 조건이 있으면 샘플 데이터에서도 동일하게 적용한다.
        list = filterOrders(list, orderId, partnerName, dueFrom, dueTo, status);
        list = limitList(list, limit);

        return ResponseEntity.ok(ok(list));
    }

    // 수주 단건 조회.
    @GetMapping("/api/orders/{orderId}")
    public ResponseEntity<Map<String, Object>> orderDetail(@PathVariable("orderId") String orderId) {
        if (isDbEnabled()) {
            Long id = parseNumericId(orderId, "ORD-");
            Map<String, Object> item = dbFindOrder(id);
            if (item == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "order not found"));
            }
            return ResponseEntity.ok(ok(item));
        }

        Map<String, Object> item = findByKey(ensureOrderStore(), "orderId", orderId);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "order not found"));
        }
        return ResponseEntity.ok(ok(item));
    }

    // 수주 등록.
    @PostMapping("/api/orders")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> body) {
        // 수주 등록 처리 흐름:
        // - 필수값(orderNo) 확인
        // - DB 가능 여부에 따라 DB 또는 메모리 저장소 사용
        String orderNo = asString(body.get("orderNo"));
        if (orderNo.isBlank()) {
            return badRequest("E-0001", "orderNo required");
        }
        String dueDate = asString(body.get("dueDate"));
        if (!dueDate.isBlank() && !isValidDate(dueDate)) {
            return badRequest("E-1001", "invalid date format");
        }
        // 초보자 설명:
        // - 수량은 숫자여야 하므로 숫자 형식인지 확인한다.
        // - 잘못된 값은 DB 저장 전에 차단한다.
        if (body.containsKey("quantity") && !isNumeric(body.get("quantity"))) {
            return badRequest("E-1004", "invalid numeric value");
        }
        if (isDbEnabled()) {
            String partnerName = asString(body.get("partnerName"));
            String status = defaultStatus(asString(body.get("status")), "PLANNED");
            // 초보자 설명:
            // - 품목 정보(코드/이름/수량)는 수주 상세에 포함되어야 한다.
            // - 화면과 레포팅에서 재사용되므로 저장/응답에 모두 포함한다.
            String productCode = asString(body.get("productCode"));
            String productName = asString(body.get("productName"));
            BigDecimal quantity = asDecimal(body.get("quantity"));
            jdbc().update(
                    "INSERT INTO orders (company_id, order_no, partner_name, due_date, status, product_code, product_name, quantity) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    DEFAULT_COMPANY_ID, orderNo, partnerName, dueDate.isBlank() ? null : dueDate, status,
                    productCode, productName, quantity);
            Long id = jdbc().queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            String orderId = id == null ? "" : formatId("ORD-", id);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("orderId", orderId);
            item.put("orderNo", orderNo);
            item.put("partnerName", partnerName);
            item.put("dueDate", dueDate);
            item.put("status", status);
            // 초보자 설명:
            // - 저장된 품목 정보를 즉시 반환해 화면이 바로 갱신되도록 한다.
            item.put("productCode", productCode);
            item.put("productName", productName);
            item.put("quantity", quantity);
            return ResponseEntity.status(HttpStatus.CREATED).body(ok(item));
        }

        String orderId = "ORD-" + String.format("%03d", orderSeq.getAndIncrement());
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("orderId", orderId);
        item.put("orderNo", orderNo);
        item.put("partnerName", asString(body.get("partnerName")));
        item.put("dueDate", asString(body.get("dueDate")));
        item.put("status", defaultStatus(asString(body.get("status")), "PLANNED"));
        item.put("productCode", asString(body.get("productCode")));
        item.put("productName", asString(body.get("productName")));
        item.put("quantity", body.get("quantity"));
        orderStore.add(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(ok(item));
    }

    // 수주 수정.
    @PutMapping("/api/orders/{orderId}")
    public ResponseEntity<Map<String, Object>> updateOrder(
            @PathVariable("orderId") String orderId,
            @RequestBody Map<String, Object> body) {
        if (isDbEnabled()) {
            Long id = parseNumericId(orderId, "ORD-");
            Map<String, Object> existing = dbFindOrder(id);
            if (existing == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "order not found"));
            }
            if (body.containsKey("dueDate")) {
                String dueDate = asString(body.get("dueDate"));
                if (!dueDate.isBlank() && !isValidDate(dueDate)) {
                    return badRequest("E-1001", "invalid date format");
                }
            }
            // 초보자 설명:
            // - 수량은 숫자만 허용한다. 숫자가 아니면 수정하지 않는다.
            if (body.containsKey("quantity") && !isNumeric(body.get("quantity"))) {
                return badRequest("E-1004", "invalid numeric value");
            }
            applyIfPresent(existing, "orderNo", body);
            applyIfPresent(existing, "partnerName", body);
            applyIfPresent(existing, "dueDate", body);
            if (body.containsKey("status")) {
                existing.put("status", defaultStatus(asString(body.get("status")), "PLANNED"));
            }
            // 초보자 설명:
            // - 품목 정보는 주문 상세의 핵심이므로 수정 대상에 포함한다.
            applyIfPresent(existing, "productCode", body);
            applyIfPresent(existing, "productName", body);
            if (body.containsKey("quantity")) {
                existing.put("quantity", asDecimal(body.get("quantity")));
            }
            jdbc().update(
                    "UPDATE orders SET order_no = ?, partner_name = ?, due_date = ?, status = ?, product_code = ?, product_name = ?, quantity = ?, updated_at = NOW() WHERE company_id = ? AND id = ?",
                    asString(existing.get("orderNo")),
                    asString(existing.get("partnerName")),
                    asString(existing.get("dueDate")).isBlank() ? null : asString(existing.get("dueDate")),
                    asString(existing.get("status")),
                    asString(existing.get("productCode")),
                    asString(existing.get("productName")),
                    asDecimal(existing.get("quantity")),
                    DEFAULT_COMPANY_ID,
                    id);
            return ResponseEntity.ok(ok(existing));
        }

        Map<String, Object> item = findByKey(ensureOrderStore(), "orderId", orderId);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "order not found"));
        }
        applyIfPresent(item, "orderNo", body);
        applyIfPresent(item, "partnerName", body);
        applyIfPresent(item, "dueDate", body);
        if (body.containsKey("status")) {
            item.put("status", defaultStatus(asString(body.get("status")), "PLANNED"));
        }
        // 초보자 설명:
        // - 메모리 모드에서도 품목 정보를 수정할 수 있어야 한다.
        applyIfPresent(item, "productCode", body);
        applyIfPresent(item, "productName", body);
        if (body.containsKey("quantity")) {
            item.put("quantity", asDecimal(body.get("quantity")));
        }
        return ResponseEntity.ok(ok(item));
    }

    // 수주 삭제.
    @DeleteMapping("/api/orders/{orderId}")
    public ResponseEntity<Map<String, Object>> deleteOrder(@PathVariable("orderId") String orderId) {
        if (isDbEnabled()) {
            Long id = parseNumericId(orderId, "ORD-");
            if (id == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "order not found"));
            }
            int updated = jdbc().update(
                    "DELETE FROM orders WHERE company_id = ? AND id = ?",
                    DEFAULT_COMPANY_ID, id);
            if (updated == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "order not found"));
            }
            return ResponseEntity.ok(ok(Map.of("deleted", true)));
        }

        boolean removed = removeByKey(ensureOrderStore(), "orderId", orderId);
        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "order not found"));
        }
        return ResponseEntity.ok(ok(Map.of("deleted", true)));
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
        // 흐름 요약:
        // 1) 날짜/상태 등 조회 조건 검증
        // 2) DB 가능 시 조건 SQL 작성
        // 3) DB 미사용 시 샘플/메모리 필터링
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

        if (isDbEnabled()) {
            List<Object> params = new ArrayList<>();
            StringBuilder sql = new StringBuilder(
                    "SELECT id, order_id, process_name, start_time, end_time, status, equipment_id, operator_name FROM jobs WHERE company_id = ?");
            params.add(DEFAULT_COMPANY_ID);

            if (jobId != null && !jobId.isBlank()) {
                Long numeric = parseNumericId(jobId, "JOB-");
                if (numeric == null) {
                    return ResponseEntity.ok(ok(new ArrayList<>()));
                }
                sql.append(" AND id = ?");
                params.add(numeric);
            }
            if (orderId != null && !orderId.isBlank()) {
                Long numeric = parseNumericId(orderId, "ORD-");
                if (numeric == null) {
                    return ResponseEntity.ok(ok(new ArrayList<>()));
                }
                sql.append(" AND order_id = ?");
                params.add(numeric);
            }
            if (processName != null && !processName.isBlank()) {
                sql.append(" AND process_name LIKE ?");
                params.add("%" + processName + "%");
            }
            if (from != null && !from.isBlank()) {
                sql.append(" AND DATE(start_time) >= ?");
                params.add(from);
            }
            if (to != null && !to.isBlank()) {
                sql.append(" AND DATE(start_time) <= ?");
                params.add(to);
            }
            if (status != null && !status.isBlank()) {
                sql.append(" AND status = ?");
                params.add(status.toUpperCase());
            }

            sql.append(" ORDER BY id DESC LIMIT ?");
            params.add(limit);

            // DB 쿼리 문자열은 null이 될 수 없도록 안전 변환을 거친다.
            List<Map<String, Object>> rows = jdbc().queryForList(requireSql(sql), params.toArray(new Object[0]));
            List<Map<String, Object>> list = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                list.add(mapJobRow(row));
            }
            return ResponseEntity.ok(ok(list));
        }

        // 샘플 파일이 없으면 1건 기본값을 만든다.
        List<Map<String, Object>> list = ensureJobStore();
        // 필터 조건이 있으면 샘플 데이터에서도 동일하게 적용한다.
        list = filterJobs(list, jobId, orderId, processName, from, to, status);
        list = limitList(list, limit);

        return ResponseEntity.ok(ok(list));
    }

    // 작업 단건 조회.
    @GetMapping("/api/jobs/{jobId}")
    public ResponseEntity<Map<String, Object>> jobDetail(@PathVariable("jobId") String jobId) {
        if (isDbEnabled()) {
            Long id = parseNumericId(jobId, "JOB-");
            Map<String, Object> item = dbFindJob(id);
            if (item == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "job not found"));
            }
            return ResponseEntity.ok(ok(item));
        }

        Map<String, Object> item = findByKey(ensureJobStore(), "jobId", jobId);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "job not found"));
        }
        return ResponseEntity.ok(ok(item));
    }

    // 작업 등록.
    @PostMapping("/api/jobs")
    public ResponseEntity<Map<String, Object>> createJob(@RequestBody Map<String, Object> body) {
        // 작업 등록 처리 흐름:
        // - 필수값(orderId, processName) 확인
        // - orderId는 "ORD-001"처럼 문자열이므로 숫자 ID로 변환 필요
        // - DB 가능 여부에 따라 DB 또는 메모리 저장소 사용
        String orderId = asString(body.get("orderId"));
        String processName = asString(body.get("processName"));
        if (orderId.isBlank() || processName.isBlank()) {
            return badRequest("E-0001", "orderId and processName required");
        }
        String startAt = asString(body.get("startAt"));
        String endAt = asString(body.get("endAt"));
        if (!startAt.isBlank() && !isValidDateTimeOrDate(startAt)) {
            return badRequest("E-1001", "invalid date format");
        }
        if (!endAt.isBlank() && !isValidDateTimeOrDate(endAt)) {
            return badRequest("E-1001", "invalid date format");
        }
        if (isDbEnabled()) {
            Long numericOrderId = parseNumericId(orderId, "ORD-");
            if (numericOrderId == null) {
                return badRequest("E-0001", "orderId invalid");
            }
            String status = defaultStatus(asString(body.get("status")), "PLANNED");
            // 초보자 설명:
            // - equipmentId: 어떤 설비에서 작업했는지 식별하는 값이다.
            // - operatorName: 작업 담당자를 기록하는 값이다.
            String equipmentId = asString(body.get("equipmentId"));
            String operatorName = asString(body.get("operatorName"));
            jdbc().update(
                    "INSERT INTO jobs (order_id, company_id, process_name, start_time, end_time, status, equipment_id, operator_name) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    numericOrderId,
                    DEFAULT_COMPANY_ID,
                    processName,
                    startAt.isBlank() ? null : startAt,
                    endAt.isBlank() ? null : endAt,
                    status,
                    equipmentId,
                    operatorName);
            Long id = jdbc().queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            String jobId = id == null ? "" : formatId("JOB-", id);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("jobId", jobId);
            item.put("orderId", orderId);
            item.put("processName", processName);
            item.put("startAt", startAt);
            item.put("endAt", endAt);
            item.put("status", status);
            // 초보자 설명:
            // - 설비/담당자 정보도 함께 반환해 화면에서 바로 표시한다.
            item.put("equipmentId", equipmentId);
            item.put("operatorName", operatorName);
            return ResponseEntity.status(HttpStatus.CREATED).body(ok(item));
        }

        String jobId = "JOB-" + String.format("%03d", jobSeq.getAndIncrement());
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("jobId", jobId);
        item.put("orderId", orderId);
        item.put("processName", processName);
        item.put("startAt", asString(body.get("startAt")));
        item.put("endAt", asString(body.get("endAt")));
        item.put("status", defaultStatus(asString(body.get("status")), "PLANNED"));
        item.put("equipmentId", asString(body.get("equipmentId")));
        item.put("operatorName", asString(body.get("operatorName")));
        jobStore.add(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(ok(item));
    }

    // 작업 수정.
    @PutMapping("/api/jobs/{jobId}")
    public ResponseEntity<Map<String, Object>> updateJob(
            @PathVariable("jobId") String jobId,
            @RequestBody Map<String, Object> body) {
        if (isDbEnabled()) {
            Long id = parseNumericId(jobId, "JOB-");
            Map<String, Object> existing = dbFindJob(id);
            if (existing == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "job not found"));
            }
            if (body.containsKey("startAt")) {
                String startAt = asString(body.get("startAt"));
                if (!startAt.isBlank() && !isValidDateTimeOrDate(startAt)) {
                    return badRequest("E-1001", "invalid date format");
                }
            }
            if (body.containsKey("endAt")) {
                String endAt = asString(body.get("endAt"));
                if (!endAt.isBlank() && !isValidDateTimeOrDate(endAt)) {
                    return badRequest("E-1001", "invalid date format");
                }
            }
            applyIfPresent(existing, "orderId", body);
            applyIfPresent(existing, "processName", body);
            applyIfPresent(existing, "startAt", body);
            applyIfPresent(existing, "endAt", body);
            if (body.containsKey("status")) {
                existing.put("status", defaultStatus(asString(body.get("status")), "PLANNED"));
            }
            // 초보자 설명:
            // - 설비/담당자 정보도 변경될 수 있으므로 수정 대상에 포함한다.
            applyIfPresent(existing, "equipmentId", body);
            applyIfPresent(existing, "operatorName", body);
            Long numericOrderId = parseNumericId(asString(existing.get("orderId")), "ORD-");
            if (numericOrderId == null) {
                return badRequest("E-0001", "orderId invalid");
            }
            jdbc().update(
                    "UPDATE jobs SET order_id = ?, process_name = ?, start_time = ?, end_time = ?, status = ?, equipment_id = ?, operator_name = ?, updated_at = NOW() WHERE company_id = ? AND id = ?",
                    numericOrderId,
                    asString(existing.get("processName")),
                    asString(existing.get("startAt")).isBlank() ? null : asString(existing.get("startAt")),
                    asString(existing.get("endAt")).isBlank() ? null : asString(existing.get("endAt")),
                    asString(existing.get("status")),
                    asString(existing.get("equipmentId")),
                    asString(existing.get("operatorName")),
                    DEFAULT_COMPANY_ID,
                    id);
            return ResponseEntity.ok(ok(existing));
        }

        Map<String, Object> item = findByKey(ensureJobStore(), "jobId", jobId);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "job not found"));
        }
        applyIfPresent(item, "orderId", body);
        applyIfPresent(item, "processName", body);
        applyIfPresent(item, "startAt", body);
        applyIfPresent(item, "endAt", body);
        if (body.containsKey("status")) {
            item.put("status", defaultStatus(asString(body.get("status")), "PLANNED"));
        }
        // 초보자 설명:
        // - 메모리 모드에서도 설비/담당자 정보를 수정할 수 있도록 한다.
        applyIfPresent(item, "equipmentId", body);
        applyIfPresent(item, "operatorName", body);
        return ResponseEntity.ok(ok(item));
    }

    // 작업 삭제.
    @DeleteMapping("/api/jobs/{jobId}")
    public ResponseEntity<Map<String, Object>> deleteJob(@PathVariable("jobId") String jobId) {
        if (isDbEnabled()) {
            Long id = parseNumericId(jobId, "JOB-");
            if (id == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "job not found"));
            }
            int updated = jdbc().update(
                    "DELETE FROM jobs WHERE company_id = ? AND id = ?",
                    DEFAULT_COMPANY_ID, id);
            if (updated == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "job not found"));
            }
            return ResponseEntity.ok(ok(Map.of("deleted", true)));
        }

        boolean removed = removeByKey(ensureJobStore(), "jobId", jobId);
        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "job not found"));
        }
        return ResponseEntity.ok(ok(Map.of("deleted", true)));
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
        // 흐름 요약:
        // 1) 기간 조건 검증
        // 2) DB 가능 시 KPI 테이블에서 조회
        // 3) DB 미사용 시 샘플/메모리에서 필터링
        if (!isValidLimit(limit)) {
            return badRequest("E-0001", "limit out of range");
        }
        if (!isValidDate(from) || !isValidDate(to)) {
            return badRequest("E-1001", "invalid date format");
        }
        if (!isValidDateRange(from, to)) {
            return badRequest("E-1003", "invalid date range");
        }

        if (isDbEnabled()) {
            List<Object> params = new ArrayList<>();
            StringBuilder sql = new StringBuilder(
                    "SELECT id, name, target_value, current_value, unit, formula, remark, kpi_date, updated_at FROM kpi WHERE company_id = ?");
            params.add(DEFAULT_COMPANY_ID);

            if (kpiId != null && !kpiId.isBlank()) {
                Long numeric = parseNumericId(kpiId, "KPI-");
                if (numeric == null) {
                    return ResponseEntity.ok(ok(new ArrayList<>()));
                }
                sql.append(" AND id = ?");
                params.add(numeric);
            }
            if (name != null && !name.isBlank()) {
                sql.append(" AND name LIKE ?");
                params.add("%" + name + "%");
            }
            if (from != null && !from.isBlank()) {
                sql.append(" AND DATE(updated_at) >= ?");
                params.add(from);
            }
            if (to != null && !to.isBlank()) {
                sql.append(" AND DATE(updated_at) <= ?");
                params.add(to);
            }

            sql.append(" ORDER BY id DESC LIMIT ?");
            params.add(limit);

            // DB 쿼리 문자열은 null이 될 수 없도록 안전 변환을 거친다.
            List<Map<String, Object>> rows = jdbc().queryForList(requireSql(sql), params.toArray(new Object[0]));
            List<Map<String, Object>> list = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                list.add(mapKpiRow(row));
            }
            return ResponseEntity.ok(ok(list));
        }

        List<Map<String, Object>> list = ensureKpiStore();
        // 필터 조건이 있으면 샘플 데이터에서도 동일하게 적용한다.
        list = filterKpi(list, name, kpiId, from, to);
        list = limitList(list, limit);

        return ResponseEntity.ok(ok(list));
    }

    // KPI 단건 조회.
    @GetMapping("/api/kpi/{kpiId}")
    public ResponseEntity<Map<String, Object>> kpiDetail(@PathVariable("kpiId") String kpiId) {
        if (isDbEnabled()) {
            Long id = parseNumericId(kpiId, "KPI-");
            Map<String, Object> item = dbFindKpi(id);
            if (item == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "kpi not found"));
            }
            return ResponseEntity.ok(ok(item));
        }

        Map<String, Object> item = findByKey(ensureKpiStore(), "kpiId", kpiId);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "kpi not found"));
        }
        return ResponseEntity.ok(ok(item));
    }

    // KPI 등록.
    @PostMapping("/api/kpi")
    public ResponseEntity<Map<String, Object>> createKpi(@RequestBody Map<String, Object> body) {
        // KPI 등록 처리 흐름:
        // - 필수값(name) 확인
        // - 숫자 필드는 BigDecimal로 변환해 정밀도 손실을 줄인다.
        // - DB 가능 여부에 따라 DB 또는 메모리 저장소 사용
        String name = asString(body.get("name"));
        if (name.isBlank()) {
            return badRequest("E-0001", "name required");
        }
        if (!isNumeric(body.get("targetValue")) || !isNumeric(body.get("currentValue"))) {
            return badRequest("E-1004", "invalid numeric value");
        }
        String kpiDate = asString(body.get("date"));
        if (!kpiDate.isBlank() && !isValidDate(kpiDate)) {
            return badRequest("E-1001", "invalid date format");
        }
        if (isDbEnabled()) {
            BigDecimal target = asDecimal(body.get("targetValue"));
            BigDecimal current = asDecimal(body.get("currentValue"));
            String unit = asString(body.get("unit"));
            String formula = asString(body.get("formula"));
            // 초보자 설명:
            // - remark: KPI에 대한 비고/설명 메모.
            // - date: KPI가 기록된 날짜(YYYY-MM-DD).
            String remark = asString(body.get("remark"));
            jdbc().update(
                    "INSERT INTO kpi (company_id, name, target_value, current_value, unit, formula, remark, kpi_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    DEFAULT_COMPANY_ID, name, target, current, unit, formula, remark, kpiDate.isBlank() ? null : kpiDate);
            Long id = jdbc().queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            String kpiId = id == null ? "" : formatId("KPI-", id);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("kpiId", kpiId);
            item.put("name", name);
            item.put("targetValue", target);
            item.put("currentValue", current);
            item.put("progressRate", calcProgressRate(target, current));
            item.put("resultValue", current);
            item.put("unit", unit);
            item.put("formula", formula);
            item.put("remark", remark);
            item.put("date", kpiDate);
            return ResponseEntity.status(HttpStatus.CREATED).body(ok(item));
        }

        String kpiId = "KPI-" + String.format("%03d", kpiSeq.getAndIncrement());
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("kpiId", kpiId);
        item.put("name", name);
        item.put("targetValue", body.get("targetValue"));
        item.put("currentValue", body.get("currentValue"));
        item.put("progressRate", body.get("progressRate"));
        item.put("resultValue", body.get("resultValue"));
        item.put("unit", asString(body.get("unit")));
        item.put("formula", asString(body.get("formula")));
        item.put("remark", asString(body.get("remark")));
        item.put("date", asString(body.get("date")));
        kpiStore.add(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(ok(item));
    }

    // KPI 수정.
    @PutMapping("/api/kpi/{kpiId}")
    public ResponseEntity<Map<String, Object>> updateKpi(
            @PathVariable("kpiId") String kpiId,
            @RequestBody Map<String, Object> body) {
        if (isDbEnabled()) {
            Long id = parseNumericId(kpiId, "KPI-");
            Map<String, Object> existing = dbFindKpi(id);
            if (existing == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "kpi not found"));
            }
            if (body.containsKey("targetValue") && !isNumeric(body.get("targetValue"))) {
                return badRequest("E-1004", "invalid numeric value");
            }
            if (body.containsKey("currentValue") && !isNumeric(body.get("currentValue"))) {
                return badRequest("E-1004", "invalid numeric value");
            }
            if (body.containsKey("date")) {
                String kpiDate = asString(body.get("date"));
                if (!kpiDate.isBlank() && !isValidDate(kpiDate)) {
                    return badRequest("E-1001", "invalid date format");
                }
            }
            applyIfPresent(existing, "name", body);
            if (body.containsKey("targetValue")) {
                existing.put("targetValue", asDecimal(body.get("targetValue")));
            }
            if (body.containsKey("currentValue")) {
                existing.put("currentValue", asDecimal(body.get("currentValue")));
            }
            applyIfPresent(existing, "unit", body);
            applyIfPresent(existing, "formula", body);
            // 초보자 설명:
            // - KPI 비고/날짜도 수정 가능하도록 반영한다.
            applyIfPresent(existing, "remark", body);
            applyIfPresent(existing, "date", body);
            BigDecimal target = asDecimal(existing.get("targetValue"));
            BigDecimal current = asDecimal(existing.get("currentValue"));
            existing.put("progressRate", calcProgressRate(target, current));
            existing.put("resultValue", current);
            jdbc().update(
                    "UPDATE kpi SET name = ?, target_value = ?, current_value = ?, unit = ?, formula = ?, remark = ?, kpi_date = ?, updated_at = NOW() WHERE company_id = ? AND id = ?",
                    asString(existing.get("name")),
                    target,
                    current,
                    asString(existing.get("unit")),
                    asString(existing.get("formula")),
                    asString(existing.get("remark")),
                    asString(existing.get("date")).isBlank() ? null : asString(existing.get("date")),
                    DEFAULT_COMPANY_ID,
                    id);
            return ResponseEntity.ok(ok(existing));
        }

        Map<String, Object> item = findByKey(ensureKpiStore(), "kpiId", kpiId);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "kpi not found"));
        }
        applyIfPresent(item, "name", body);
        applyIfPresent(item, "targetValue", body);
        applyIfPresent(item, "currentValue", body);
        applyIfPresent(item, "progressRate", body);
        applyIfPresent(item, "resultValue", body);
        applyIfPresent(item, "unit", body);
        applyIfPresent(item, "formula", body);
        applyIfPresent(item, "remark", body);
        applyIfPresent(item, "date", body);
        return ResponseEntity.ok(ok(item));
    }

    // KPI 삭제.
    @DeleteMapping("/api/kpi/{kpiId}")
    public ResponseEntity<Map<String, Object>> deleteKpi(@PathVariable("kpiId") String kpiId) {
        if (isDbEnabled()) {
            Long id = parseNumericId(kpiId, "KPI-");
            if (id == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "kpi not found"));
            }
            int updated = jdbc().update(
                    "DELETE FROM kpi WHERE company_id = ? AND id = ?",
                    DEFAULT_COMPANY_ID, id);
            if (updated == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "kpi not found"));
            }
            return ResponseEntity.ok(ok(Map.of("deleted", true)));
        }

        boolean removed = removeByKey(ensureKpiStore(), "kpiId", kpiId);
        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fail("E-404", "kpi not found"));
        }
        return ResponseEntity.ok(ok(Map.of("deleted", true)));
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
        // 흐름 요약:
        // 1) 기간 조건 검증
        // 2) DB 가능 시 KPI 추이 테이블에서 조회
        // 3) DB 미사용 시 샘플/메모리 데이터 사용
        if (!isValidLimit(limit)) {
            return badRequest("E-0001", "limit out of range");
        }
        if (!isValidDate(from) || !isValidDate(to)) {
            return badRequest("E-1001", "invalid date format");
        }
        if (!isValidDateRange(from, to)) {
            return badRequest("E-1003", "invalid date range");
        }

        if (isDbEnabled()) {
            List<Object> params = new ArrayList<>();
            StringBuilder sql = new StringBuilder(
                    "SELECT t.date, t.target_value, t.current_value, k.id AS kpi_id, k.name AS kpi_name " +
                    "FROM kpi_trend t JOIN kpi k ON t.kpi_id = k.id WHERE k.company_id = ?");
            params.add(DEFAULT_COMPANY_ID);
            if (kpiId != null && !kpiId.isBlank()) {
                Long numeric = parseNumericId(kpiId, "KPI-");
                if (numeric == null) {
                    return ResponseEntity.ok(ok(new ArrayList<>()));
                }
                sql.append(" AND k.id = ?");
                params.add(numeric);
            }
            if (from != null && !from.isBlank()) {
                sql.append(" AND t.date >= ?");
                params.add(from);
            }
            if (to != null && !to.isBlank()) {
                sql.append(" AND t.date <= ?");
                params.add(to);
            }
            sql.append(" ORDER BY t.date DESC LIMIT ?");
            params.add(limit);

            // DB 쿼리 문자열은 null이 될 수 없도록 안전 변환을 거친다.
            List<Map<String, Object>> rows = jdbc().queryForList(requireSql(sql), params.toArray(new Object[0]));
            List<Map<String, Object>> list = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                Map<String, Object> item = new LinkedHashMap<>();
                Long id = asLong(row.get("kpi_id"));
                item.put("kpiId", id == null ? "" : formatId("KPI-", id));
                item.put("name", asString(row.get("kpi_name")));
                item.put("date", asString(row.get("date")));
                item.put("targetValue", asDecimal(row.get("target_value")));
                item.put("currentValue", asDecimal(row.get("current_value")));
                list.add(item);
            }
            return ResponseEntity.ok(ok(list));
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
        // 초보자 설명:
        // - result는 성공 여부, message는 안내 문구, data는 실제 데이터다.
        // - 화면이나 다른 시스템은 이 구조만 보고 처리한다.
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
        // 초보자 설명:
        // - 잘못된 입력(형식 오류 등)은 400으로 돌려준다.
        // - errorCode는 화면에서 "어떤 오류인지" 구분하는 데 사용한다.
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("result", "FAIL");
        res.put("message", message);
        res.put("errorCode", errorCode);
        res.put("data", null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    // 공통 실패 응답 포맷(404 등).
    private Map<String, Object> fail(String errorCode, String message) {
        // 초보자 설명:
        // - 데이터가 없을 때(404 등) 사용하는 공통 구조다.
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("result", "FAIL");
        res.put("message", message);
        res.put("errorCode", errorCode);
        res.put("data", null);
        return res;
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

    // 연계 상태 값 검증.
    // 이유: 외부기관 연계 조회는 ACCEPTED/FAILED만 허용한다.
    private boolean isValidSyncStatus(String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        String upper = status.toUpperCase();
        return "ACCEPTED".equals(upper) || "FAILED".equals(upper);
    }

    // 날짜 형식(YYYY-MM-DD)만 허용한다.
    // 이유: 검색 조건이 일관된 형식을 갖추어야 비교가 가능하다.
    private boolean isValidDate(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        if (!value.matches("\\d{4}-\\d{2}-\\d{2}")) {
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

    // 날짜 또는 날짜+시간 형식을 허용한다.
    // 이유: 작업 시작/종료 시간은 "YYYY-MM-DD" 또는 "YYYY-MM-DDTHH:MM:SS"로 올 수 있다.
    private boolean isValidDateTimeOrDate(String value) {
        // 초보자 설명:
        // - 날짜만 들어올 수도 있고, 날짜+시간이 들어올 수도 있다.
        // - 두 형식 중 하나라도 맞으면 통과시키는 보조 검증이다.
        if (value == null || value.isBlank()) {
            return true;
        }
        if (value.length() == 10) {
            return isValidDate(value);
        }
        if (!value.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")) {
            return false;
        }
        String date = value.substring(0, 10);
        return isValidDate(date);
    }

    // 숫자 형식인지 확인한다.
    // 이유: KPI 목표값/현재값은 숫자만 허용해야 한다.
    private boolean isNumeric(Object value) {
        // 초보자 설명:
        // - 숫자 입력이 필요한 곳에서 문자열/숫자 모두 허용한다.
        // - 잘못된 값이면 false를 반환해 에러코드로 안내한다.
        if (value == null) {
            return true;
        }
        if (value instanceof Number) {
            return true;
        }
        String text = asString(value);
        if (text.isBlank()) {
            return true;
        }
        return text.matches("-?\\\\d+(\\\\.\\\\d+)?");
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

    // 장비 저장소 초기화.
    private List<Map<String, Object>> ensureEquipmentStore() {
        if (equipmentStore.isEmpty()) {
            List<Map<String, Object>> list = sampleList("samples/equipments.json");
            if (list == null || list.isEmpty()) {
                list = new ArrayList<>();
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("deviceId", "EQ-001");
                item.put("name", "샘플 설비");
                item.put("lastSeenAt", "2025-12-24T00:00:00");
                item.put("status", "ACTIVE");
                list.add(item);
            }
            equipmentStore.addAll(list);
        }
        return equipmentStore;
    }

    // 수주 저장소 초기화.
    private List<Map<String, Object>> ensureOrderStore() {
        if (orderStore.isEmpty()) {
            List<Map<String, Object>> list = sampleList("samples/orders.json");
            if (list == null || list.isEmpty()) {
                list = new ArrayList<>();
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("orderId", "ORD-001");
                item.put("orderNo", "ORD-001");
                item.put("partnerName", "샘플 거래처");
                item.put("productCode", "ITEM-001");
                item.put("productName", "샘플 품목");
                item.put("quantity", 10);
                item.put("dueDate", "2025-12-31");
                item.put("status", "PLANNED");
                list.add(item);
            }
            orderStore.addAll(list);
        }
        return orderStore;
    }

    // 작업 저장소 초기화.
    private List<Map<String, Object>> ensureJobStore() {
        if (jobStore.isEmpty()) {
            List<Map<String, Object>> list = sampleList("samples/jobs.json");
            if (list == null || list.isEmpty()) {
                list = new ArrayList<>();
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("jobId", "JOB-001");
                item.put("orderId", "ORD-001");
                item.put("processName", "Cutting");
                item.put("equipmentId", "EQ-001");
                item.put("operatorName", "작업자A");
                item.put("startAt", "2025-12-24T09:00:00");
                item.put("endAt", "2025-12-24T12:00:00");
                item.put("status", "DONE");
                list.add(item);
            }
            jobStore.addAll(list);
        }
        return jobStore;
    }

    // KPI 저장소 초기화.
    private List<Map<String, Object>> ensureKpiStore() {
        if (kpiStore.isEmpty()) {
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
            kpiStore.addAll(list);
        }
        return kpiStore;
    }

    // 외부기관 연계 이력 저장소 초기화.
    // 이유: DB가 없어도 조회 화면 흐름을 검증해야 하기 때문이다.
    private List<Map<String, Object>> ensureExternalSyncStore() {
        if (externalSyncStore.isEmpty()) {
            List<Map<String, Object>> list = sampleList("samples/external-sync-logs.json");
            if (list == null || list.isEmpty()) {
                list = new ArrayList<>();
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("time", "2025-12-24T09:10:00");
                item.put("requestId", "SYNC-001");
                item.put("status", "ACCEPTED");
                item.put("acceptedAt", "2025-12-24T09:10:30");
                item.put("message", "sample accepted");
                item.put("agency", "AgencyA");
                list.add(item);

                Map<String, Object> item2 = new LinkedHashMap<>();
                item2.put("time", "2025-12-23T16:40:00");
                item2.put("requestId", "SYNC-002");
                item2.put("status", "FAILED");
                item2.put("acceptedAt", "");
                item2.put("message", "sample failed");
                item2.put("agency", "AgencyB");
                list.add(item2);
            }
            externalSyncStore.addAll(list);
        }
        return externalSyncStore;
    }

    // 단일 키 기준으로 항목을 찾는다.
    private Map<String, Object> findByKey(List<Map<String, Object>> list, String key, String value) {
        for (Map<String, Object> item : list) {
            if (value.equals(asString(item.get(key)))) {
                return item;
            }
        }
        return null;
    }

    // 단일 키 기준으로 항목을 제거한다.
    private boolean removeByKey(List<Map<String, Object>> list, String key, String value) {
        Map<String, Object> target = null;
        for (Map<String, Object> item : list) {
            if (value.equals(asString(item.get(key)))) {
                target = item;
                break;
            }
        }
        if (target == null) {
            return false;
        }
        return list.remove(target);
    }

    // 요청 바디에 값이 있으면 기존 항목에 반영한다.
    private void applyIfPresent(Map<String, Object> target, String key, Map<String, Object> body) {
        if (!body.containsKey(key)) {
            return;
        }
        Object value = body.get(key);
        if (value != null && !asString(value).isBlank()) {
            target.put(key, value);
        }
    }

    // 상태 기본값을 보정한다.
    private String defaultStatus(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.toUpperCase();
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

    // 외부기관 연계 이력 필터를 적용한다.
    // 이유: 화면 필터 값이 API 결과에 반영되는지 확인하기 위함이다.
    private List<Map<String, Object>> filterExternalSyncLogs(List<Map<String, Object>> list,
            String requestId, String agency, String from, String to, String status, String keyword) {
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> item : list) {
            if (!matchContains(item.get("requestId"), requestId)) {
                continue;
            }
            if (!matchContains(item.get("agency"), agency)) {
                continue;
            }
            if (!matchDateRange(item.get("time"), from, to)) {
                continue;
            }
            if (!matchEquals(item.get("status"), status)) {
                continue;
            }
            if (!matchContains(item.get("message"), keyword)) {
                continue;
            }
            filtered.add(item);
        }
        return filtered;
    }

    // DB 사용 가능 여부를 반환한다.
    // 이유: 접속 정보가 없으면 기존 메모리 흐름을 그대로 유지해야 한다.
    private boolean isDbEnabled() {
        // 초보자 설명:
        // - DB 접속 정보가 모두 준비된 경우에만 true가 된다.
        // - false이면 메모리 저장소를 사용한다.
        // DB 지원 객체가 있고, 접속 정보가 모두 있을 때만 true
        return dbSupport != null && dbSupport.isEnabled();
    }

    // JdbcTemplate을 반환한다.
    // 이유: DB 접근 코드를 한 곳에서 관리하기 위함이다.
    private JdbcTemplate jdbc() {
        // 초보자 설명:
        // - DB 연결 객체를 가져오는 함수다.
        // - DB 미사용 상태에서는 호출되지 않는다고 가정한다.
        // DB 연결이 활성화된 경우에만 호출된다는 전제
        return dbSupport.jdbc();
    }

    // 공통 ID 포맷을 만든다.
    // 이유: 화면/계약에서 사용하는 문자열 ID 규칙을 유지하기 위함이다.
    private String formatId(String prefix, long id) {
        // 예: ("ORD-", 1) -> "ORD-001"
        return prefix + String.format("%03d", id);
    }

    // 문자열 ID에서 숫자 ID를 추출한다.
    // 이유: "ORD-001" 같은 값을 DB의 숫자 ID로 변환해야 하기 때문이다.
    private Long parseNumericId(String value, String prefix) {
        // "ORD-001" -> 1, "001" -> 1
        // 규칙에 맞지 않으면 null 반환
        // 초보자 설명:
        // - 화면에서 쓰는 문자열 ID를 DB용 숫자로 바꾸는 과정이다.
        // - 규칙에 맞지 않으면 null을 반환해 오류를 알린다.
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        String numeric = null;
        if (trimmed.startsWith(prefix)) {
            numeric = trimmed.substring(prefix.length());
        } else if (trimmed.matches("\\\\d+")) {
            numeric = trimmed;
        }
        if (numeric == null || numeric.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(numeric);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    // 숫자 값을 BigDecimal로 변환한다.
    // 이유: DB에서 가져온 값이 다양한 타입일 수 있어 안전한 변환이 필요하다.
    private BigDecimal asDecimal(Object value) {
        // 숫자를 안전하게 BigDecimal로 변환한다.
        // 잘못된 값이면 null로 반환해 이후 검증/저장 로직이 처리하도록 한다.
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    // 숫자 값을 Long으로 변환한다.
    private Long asLong(Object value) {
        // 숫자를 안전하게 Long으로 변환한다.
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    // 장비 DB 행을 API 응답 형태로 변환한다.
    private Map<String, Object> mapEquipmentRow(Map<String, Object> row) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("deviceId", asString(row.get("device_code")));
        item.put("name", asString(row.get("name")));
        item.put("model", asString(row.get("model")));
        item.put("vendor", asString(row.get("vendor")));
        item.put("lastSeenAt", asString(row.get("updated_at")));
        item.put("status", defaultStatus(asString(row.get("status")), "ACTIVE"));
        return item;
    }

    // 수주 DB 행을 API 응답 형태로 변환한다.
    private Map<String, Object> mapOrderRow(Map<String, Object> row) {
        Long id = asLong(row.get("id"));
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("orderId", id == null ? "" : formatId("ORD-", id));
        item.put("orderNo", asString(row.get("order_no")));
        item.put("partnerName", asString(row.get("partner_name")));
        item.put("dueDate", asString(row.get("due_date")));
        item.put("status", defaultStatus(asString(row.get("status")), "PLANNED"));
        // 초보자 설명:
        // - 품목 정보는 수주 화면에서 반드시 보여야 하므로 응답에 포함한다.
        item.put("productCode", asString(row.get("product_code")));
        item.put("productName", asString(row.get("product_name")));
        item.put("quantity", asDecimal(row.get("quantity")));
        return item;
    }

    // 작업 DB 행을 API 응답 형태로 변환한다.
    private Map<String, Object> mapJobRow(Map<String, Object> row) {
        Long id = asLong(row.get("id"));
        Long orderId = asLong(row.get("order_id"));
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("jobId", id == null ? "" : formatId("JOB-", id));
        item.put("orderId", orderId == null ? "" : formatId("ORD-", orderId));
        item.put("processName", asString(row.get("process_name")));
        item.put("startAt", asString(row.get("start_time")));
        item.put("endAt", asString(row.get("end_time")));
        item.put("status", defaultStatus(asString(row.get("status")), "PLANNED"));
        // 초보자 설명:
        // - 설비/담당자 정보는 작업 추적에 필요하므로 응답에 포함한다.
        item.put("equipmentId", asString(row.get("equipment_id")));
        item.put("operatorName", asString(row.get("operator_name")));
        return item;
    }

    // KPI DB 행을 API 응답 형태로 변환한다.
    private Map<String, Object> mapKpiRow(Map<String, Object> row) {
        // 초보자 설명:
        // - DB에서 읽어온 KPI 데이터를 화면이 이해할 수 있는 구조로 바꾼다.
        // - 숫자/문자 타입을 안전하게 변환해 화면 오류를 줄인다.
        Long id = asLong(row.get("id"));
        BigDecimal target = asDecimal(row.get("target_value"));
        BigDecimal current = asDecimal(row.get("current_value"));
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("kpiId", id == null ? "" : formatId("KPI-", id));
        item.put("name", asString(row.get("name")));
        item.put("targetValue", target);
        item.put("currentValue", current);
        item.put("progressRate", calcProgressRate(target, current));
        item.put("resultValue", current);
        item.put("unit", asString(row.get("unit")));
        item.put("formula", asString(row.get("formula")));
        item.put("remark", asString(row.get("remark")));
        item.put("date", asString(row.get("kpi_date")));
        return item;
    }

    // SQL 문자열을 안전하게 반환한다.
    // 이유: Null safety 경고를 방지하고, 실행 시에도 null SQL이 전달되지 않게 한다.
    private @NonNull String requireSql(@NonNull StringBuilder sql) {
        // 초보자 설명:
        // - SQL 문자열이 null이 되지 않도록 안전하게 처리한다.
        // - IDE 경고를 줄이고, 실행 안정성을 높인다.
        return Objects.requireNonNull(sql.toString());
    }

    // 진행률을 계산한다.
    private Double calcProgressRate(BigDecimal target, BigDecimal current) {
        if (target == null || current == null) {
            return null;
        }
        if (target.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return current.divide(target, 4, RoundingMode.HALF_UP).doubleValue();
    }

    // 장비 단건 조회(DB).
    private Map<String, Object> dbFindEquipment(String deviceId) {
        List<Map<String, Object>> rows = jdbc().queryForList(
                "SELECT id, device_code, name, model, vendor, status, updated_at FROM equipment WHERE company_id = ? AND device_code = ?",
                DEFAULT_COMPANY_ID, deviceId);
        if (rows.isEmpty()) {
            return null;
        }
        return mapEquipmentRow(rows.get(0));
    }

    // 수주 단건 조회(DB).
    private Map<String, Object> dbFindOrder(Long id) {
        if (id == null) {
            return null;
        }
        List<Map<String, Object>> rows = jdbc().queryForList(
                "SELECT id, order_no, partner_name, due_date, status, product_code, product_name, quantity FROM orders WHERE company_id = ? AND id = ?",
                DEFAULT_COMPANY_ID, id);
        if (rows.isEmpty()) {
            return null;
        }
        return mapOrderRow(rows.get(0));
    }

    // 작업 단건 조회(DB).
    private Map<String, Object> dbFindJob(Long id) {
        if (id == null) {
            return null;
        }
        List<Map<String, Object>> rows = jdbc().queryForList(
                "SELECT id, order_id, process_name, start_time, end_time, status, equipment_id, operator_name FROM jobs WHERE company_id = ? AND id = ?",
                DEFAULT_COMPANY_ID, id);
        if (rows.isEmpty()) {
            return null;
        }
        return mapJobRow(rows.get(0));
    }

    // KPI 단건 조회(DB).
    private Map<String, Object> dbFindKpi(Long id) {
        if (id == null) {
            return null;
        }
        List<Map<String, Object>> rows = jdbc().queryForList(
                "SELECT id, name, target_value, current_value, unit, formula, remark, kpi_date, updated_at FROM kpi WHERE company_id = ? AND id = ?",
                DEFAULT_COMPANY_ID, id);
        if (rows.isEmpty()) {
            return null;
        }
        return mapKpiRow(rows.get(0));
    }
}
