package com.mes.middleware.pipeline;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mes.common.logging.PassFailLog;
import com.mes.middleware.storage.NormalizedStore;
import com.mes.middleware.storage.QuarantineStore;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RawPipelineService {
    // 초보자 설명:
    // - 이 서비스는 "들어온 원본 데이터"를 간단히 분류하고 저장 위치를 결정한다.
    // - 확신이 낮으면 격리, 높으면 정규화 저장으로 보낸다.
    // 목적: 확신이 낮은 데이터는 격리 폴더에 저장한다.
    // 이유: 잘못된 파싱/정규화로 데이터가 오염되는 것을 막기 위해서다.
    private final QuarantineStore quarantineStore;
    // 목적: 확신이 충분한 데이터는 정규화 결과로 저장한다.
    // 이유: 화면/분석에서 사용할 표준 형태를 확보하기 위해서다.
    private final NormalizedStore normalizedStore;
    // 목적: JSON 파싱을 안전하게 수행하기 위한 공용 파서다.
    // 이유: 매 요청마다 파서를 생성하면 비용이 커지고 오류 처리도 분산되기 때문이다.
    private static final ObjectMapper JSON = new ObjectMapper();

    public RawPipelineService(QuarantineStore quarantineStore, NormalizedStore normalizedStore) {
        // 의존성 주입으로 저장소를 연결한다.
        this.quarantineStore = quarantineStore;
        this.normalizedStore = normalizedStore;
    }

    // 목적: 원본 데이터를 최소 분류해 정규화 또는 격리로 분기한다.
    // 이유: P1 단계에서 자동 식별/파싱의 최소 동작 경로를 확보하기 위함이다.
    // 입력: rawId(원본 식별자), payload(원본 문자열).
    // 출력: 없음(저장소에 저장 + 로그만 남김).
    public void process(String rawId, String payload) {
        String safePayload = payload == null ? "" : payload;
        ClassificationResult result = classify(safePayload);
        ValidationDecision decision = validate(result, safePayload);
        if (decision == ValidationDecision.QUARANTINE) {
            quarantineStore.save(rawId, safePayload);
            PassFailLog.skip("quarantine " + rawId);
            return;
        }
        NormalizedStore.NormalizedRecord record = buildRecord(rawId, safePayload, result);
        record.validationStatus = decision.name();
        normalizedStore.save(record);
        PassFailLog.pass("normalized " + rawId);
    }

    // 목적: 데이터 형식을 추정한다.
    // 이유: 상세 프로토콜이 없더라도 최소한의 분류 기준을 확보하기 위함이다.
    // 입력: payload 문자열.
    // 출력: 분류 결과(프로토콜/포맷/신뢰도).
    private ClassificationResult classify(String payload) {
        String trimmed = payload.trim();
        if (trimmed.isEmpty()) {
            return new ClassificationResult("UNKNOWN", "empty", "unknown", 0.0);
        }
        if (looksLikeJson(trimmed)) {
            return new ClassificationResult("UNKNOWN", "json", "measurement", 0.9);
        }
        if (looksLikeCsv(trimmed)) {
            return new ClassificationResult("UNKNOWN", "csv", "measurement", 0.6);
        }
        if (looksBinary(trimmed)) {
            return new ClassificationResult("UNKNOWN", "binary", "unknown", 0.3);
        }
        return new ClassificationResult("UNKNOWN", "text", "status", 0.5);
    }

    // 목적: 정규화 저장에 필요한 기본 레코드를 만든다.
    // 이유: 분류 결과와 원본을 함께 묶어 저장 규칙을 단순화하기 위함이다.
    private NormalizedStore.NormalizedRecord buildRecord(String rawId, String payload, ClassificationResult result) {
        NormalizedStore.NormalizedRecord record = new NormalizedStore.NormalizedRecord();
        record.rawId = rawId;
        record.deviceHint = extractDeviceHint(payload, result.format);
        record.protocolHint = result.protocolHint;
        record.format = result.format;
        record.eventType = extractEventType(payload, result.format, result.eventType);
        record.eventTime = extractEventTime(payload, result.format);
        record.confidence = result.confidence;
        record.payloadJson = toPayloadJson(payload, result.format);
        record.createdAt = OffsetDateTime.now().toString();
        return record;
    }

    // 목적: 원본 payload를 JSON 문자열로 감싼다.
    // 이유: 정규화 저장은 JSON을 기준으로 처리하기 쉽기 때문이다.
    private String toPayloadJson(String payload, String format) {
        String trimmed = payload == null ? "" : payload.trim();
        if ("json".equals(format)) {
            return trimmed.isEmpty() ? "{}" : trimmed;
        }
        String safe = trimmed.replace("\\", "\\\\").replace("\"", "\\\"");
        return "{\"raw\":\"" + safe + "\"}";
    }

    private boolean looksLikeJson(String trimmed) {
        return (trimmed.startsWith("{") && trimmed.endsWith("}"))
                || (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }

    private boolean looksLikeCsv(String trimmed) {
        return trimmed.contains(",") || trimmed.contains(";") || trimmed.contains("\t");
    }

    private boolean looksBinary(String trimmed) {
        int len = trimmed.length();
        if (len == 0) {
            return false;
        }
        int nonPrintable = 0;
        for (int i = 0; i < len; i++) {
            char c = trimmed.charAt(i);
            if (c < 0x09 || (c > 0x0D && c < 0x20)) {
                nonPrintable++;
            }
        }
        double ratio = (double) nonPrintable / (double) len;
        return ratio > 0.2;
    }

    // 목적: payload 안에서 deviceId 값을 최대한 찾아낸다.
    // 이유: 장비 식별이 가능하면 이후 데이터 매핑 정확도가 올라간다.
    private String extractDeviceHint(String payload, String format) {
        // 초보자 설명:
        // - 원본 데이터 안에 "deviceId"가 있으면 그 값을 장비 힌트로 사용한다.
        // - 없으면 UNKNOWN으로 두어 후속 단계에서 다시 판단할 수 있게 한다.
        String safe = payload == null ? "" : payload;
        String fromJson = extractFromJson(safe, format,
                new String[] { "deviceId", "device_id", "deviceID", "deviceid" });
        if (isPresent(fromJson)) {
            return fromJson;
        }
        String fromCsv = extractFromCsv(safe, format,
                new String[] { "deviceId", "device_id", "device" });
        if (isPresent(fromCsv)) {
            return fromCsv;
        }
        Pattern p = Pattern.compile("\"deviceId\"\\s*:\\s*\"([^\"]+)\"|\"device_id\"\\s*:\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(safe);
        if (m.find()) {
            String v1 = m.group(1);
            String v2 = m.group(2);
            if (isPresent(v1)) {
                return v1;
            }
            if (isPresent(v2)) {
                return v2;
            }
        }
        return "UNKNOWN";
    }

    // 목적: payload 안에서 eventTime 값을 최대한 찾아낸다.
    // 이유: 시간 정보가 있어야 정규화 결과의 순서와 분석 기준이 맞춰지기 때문이다.
    private String extractEventTime(String payload, String format) {
        String safe = payload == null ? "" : payload;
        String fromJson = extractFromJson(safe, format,
                new String[] { "eventTime", "event_time", "timestamp", "ts", "time" });
        String normalized = normalizeTime(fromJson);
        if (isPresent(normalized)) {
            return normalized;
        }
        String fromCsv = extractFromCsv(safe, format,
                new String[] { "eventTime", "event_time", "timestamp", "time" });
        normalized = normalizeTime(fromCsv);
        if (isPresent(normalized)) {
            return normalized;
        }
        // 시간이 없는 경우는 현재 시각으로 보정한다.
        return OffsetDateTime.now().toString();
    }

    // 목적: payload 안에서 eventType 값을 최대한 찾아낸다.
    // 이유: 이벤트 성격을 알 수 있으면 후속 매핑 로직이 단순해지기 때문이다.
    private String extractEventType(String payload, String format, String fallback) {
        String safe = payload == null ? "" : payload;
        String fromJson = extractFromJson(safe, format,
                new String[] { "eventType", "event_type", "type", "dataType" });
        if (isPresent(fromJson)) {
            return fromJson;
        }
        String fromCsv = extractFromCsv(safe, format,
                new String[] { "eventType", "event_type", "type" });
        if (isPresent(fromCsv)) {
            return fromCsv;
        }
        return fallback == null ? "unknown" : fallback;
    }

    // 목적: JSON 포맷에서 지정 키의 값을 추출한다.
    // 이유: 장비 힌트/시간/유형을 JSON에서 가장 먼저 찾는 것이 정확도가 높기 때문이다.
    private String extractFromJson(String payload, String format, String[] keys) {
        if (!"json".equals(format)) {
            return "";
        }
        JsonNode root = parseJson(payload);
        if (root == null) {
            return "";
        }
        for (String key : keys) {
            JsonNode node = root.get(key);
            if (node != null && !node.isNull()) {
                String value = node.asText();
                if (isPresent(value)) {
                    return value;
                }
            }
        }
        return "";
    }

    // 목적: CSV 포맷에서 지정 키의 값을 추출한다.
    // 이유: CSV는 첫 줄 헤더/둘째 줄 값 형태가 많아 간단 매핑으로 힌트를 얻을 수 있다.
    private String extractFromCsv(String payload, String format, String[] keys) {
        if (!"csv".equals(format)) {
            return "";
        }
        CsvView view = parseCsv(payload);
        if (view == null || view.headers == null || view.values == null) {
            return "";
        }
        for (String key : keys) {
            int idx = view.indexOf(key);
            if (idx >= 0 && idx < view.values.length) {
                String value = view.values[idx];
                if (isPresent(value)) {
                    return value;
                }
            }
        }
        return "";
    }

    // 목적: JSON 문자열을 안전하게 파싱한다.
    // 이유: 잘못된 JSON이 들어와도 서비스가 중단되지 않게 하기 위함이다.
    private JsonNode parseJson(String payload) {
        try {
            return JSON.readTree(payload);
        } catch (Exception ex) {
            return null;
        }
    }

    // 목적: CSV에서 헤더/값 구조를 간단히 파싱한다.
    // 이유: 복잡한 CSV 파서 없이도 최소 힌트를 얻어 PR-B2 목표를 달성하기 위함이다.
    private CsvView parseCsv(String payload) {
        if (payload == null) {
            return null;
        }
        String trimmed = payload.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String[] lines = trimmed.split("\\r?\\n");
        if (lines.length < 2) {
            return null;
        }
        String delimiter = detectDelimiter(lines[0]);
        String[] headers = splitCsvLine(lines[0], delimiter);
        String[] values = splitCsvLine(lines[1], delimiter);
        return new CsvView(headers, values);
    }

    // 목적: CSV 구분자를 단순 판별한다.
    // 이유: 탭/세미콜론/콤마가 혼재할 수 있어 기본 우선순위를 둔다.
    private String detectDelimiter(String line) {
        if (line.contains("\t")) {
            return "\t";
        }
        if (line.contains(";")) {
            return ";";
        }
        return ",";
    }

    // 목적: CSV 한 줄을 분리한다.
    // 이유: 최소 구현으로 헤더/값을 분리해 힌트 추출에 사용하기 위함이다.
    private String[] splitCsvLine(String line, String delimiter) {
        String[] raw = line.split(Pattern.quote(delimiter), -1);
        for (int i = 0; i < raw.length; i++) {
            raw[i] = raw[i].trim();
        }
        return raw;
    }

    // 목적: 문자열이 유효한지 확인한다.
    // 이유: 공백/빈 값은 힌트로 사용할 수 없기 때문이다.
    private boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    // 목적: 시간 문자열을 표준 포맷으로 맞춘다.
    // 이유: 숫자 타임스탬프/문자 시간 모두를 같은 필드에서 다루기 위함이다.
    private String normalizeTime(String raw) {
        if (!isPresent(raw)) {
            return "";
        }
        String trimmed = raw.trim();
        Long epoch = parseEpoch(trimmed);
        if (epoch == null) {
            return trimmed;
        }
        Instant instant = epoch >= 1_000_000_000_000L
                ? Instant.ofEpochMilli(epoch)
                : Instant.ofEpochSecond(epoch);
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC).toString();
    }

    // 목적: 숫자 형태의 시간값을 파싱한다.
    // 이유: 숫자 외 문자열은 그대로 유지해야 하기 때문이다.
    private Long parseEpoch(String value) {
        String normalized = value.replaceAll("[^0-9]", "");
        if (normalized.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static final class CsvView {
        private final String[] headers;
        private final String[] values;

        private CsvView(String[] headers, String[] values) {
            this.headers = headers;
            this.values = values;
        }

        private int indexOf(String key) {
            String target = key.toLowerCase(Locale.ROOT);
            for (int i = 0; i < headers.length; i++) {
                if (headers[i] == null) {
                    continue;
                }
                if (headers[i].toLowerCase(Locale.ROOT).equals(target)) {
                    return i;
                }
            }
            return -1;
        }
    }

    // 목적: 분류 결과와 payload를 검증해 승인/보류/격리를 결정한다.
    // 이유: 자동 적재 전에 기본 품질을 확인해야 데이터 오염을 줄일 수 있다.
    private ValidationDecision validate(ClassificationResult result, String payload) {
        if (payload == null || payload.trim().isEmpty()) {
            return ValidationDecision.QUARANTINE;
        }
        if ("json".equals(result.format) && !looksLikeJson(payload.trim())) {
            return ValidationDecision.QUARANTINE;
        }
        if (result.confidence < 0.5) {
            return ValidationDecision.QUARANTINE;
        }
        if (result.confidence < 0.7) {
            return ValidationDecision.HOLD;
        }
        return ValidationDecision.APPROVED;
    }

    private enum ValidationDecision {
        APPROVED,
        HOLD,
        QUARANTINE
    }

    public static final class ClassificationResult {
        // 목적: 분류 결과를 한 번에 묶어 전달한다.
        // 이유: 포맷/프로토콜/신뢰도를 함께 사용하기 때문이다.
        public final String protocolHint;
        public final String format;
        public final String eventType;
        public final double confidence;

        public ClassificationResult(String protocolHint, String format, String eventType, double confidence) {
            // 분류 결과를 그대로 보관한다.
            this.protocolHint = protocolHint;
            this.format = format;
            this.eventType = eventType;
            this.confidence = confidence;
        }
    }
}
