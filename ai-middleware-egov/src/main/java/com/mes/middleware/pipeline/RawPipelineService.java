package com.mes.middleware.pipeline;

import org.springframework.stereotype.Service;

import com.mes.common.logging.PassFailLog;
import com.mes.middleware.storage.NormalizedStore;
import com.mes.middleware.storage.QuarantineStore;

@Service
public class RawPipelineService {
    // 목적: 확신이 낮은 데이터는 격리 폴더에 저장한다.
    // 이유: 잘못된 파싱/정규화로 데이터가 오염되는 것을 막기 위해서다.
    private final QuarantineStore quarantineStore;
    // 목적: 확신이 충분한 데이터는 정규화 결과로 저장한다.
    // 이유: 화면/분석에서 사용할 표준 형태를 확보하기 위해서다.
    private final NormalizedStore normalizedStore;

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
        record.deviceHint = "UNKNOWN";
        record.protocolHint = result.protocolHint;
        record.format = result.format;
        record.eventType = result.eventType;
        record.eventTime = java.time.OffsetDateTime.now().toString();
        record.confidence = result.confidence;
        record.payloadJson = toPayloadJson(payload, result.format);
        record.createdAt = java.time.OffsetDateTime.now().toString();
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
