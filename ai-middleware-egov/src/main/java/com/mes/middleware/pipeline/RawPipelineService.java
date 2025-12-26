package com.mes.middleware.pipeline;

import org.springframework.stereotype.Service;

import com.mes.common.logging.PassFailLog;
import com.mes.middleware.storage.NormalizedStore;
import com.mes.middleware.storage.QuarantineStore;

@Service
public class RawPipelineService {
    private final QuarantineStore quarantineStore;
    private final NormalizedStore normalizedStore;

    public RawPipelineService(QuarantineStore quarantineStore, NormalizedStore normalizedStore) {
        this.quarantineStore = quarantineStore;
        this.normalizedStore = normalizedStore;
    }

    // 목적: 원본 데이터를 최소 분류해 정규화 또는 격리로 분기한다.
    // 이유: P1 단계에서 자동 식별/파싱의 최소 동작 경로를 확보하기 위함이다.
    public void process(String rawId, String payload) {
        ClassificationResult result = classify(payload == null ? "" : payload);
        if (result.confidence < 0.5) {
            quarantineStore.save(rawId, payload);
            PassFailLog.skip("quarantine " + rawId);
            return;
        }
        normalizedStore.save(rawId, result);
        PassFailLog.pass("normalized " + rawId);
    }

    // 목적: 데이터 형식을 추정한다.
    // 이유: 상세 프로토콜이 없더라도 최소한의 분류 기준을 확보하기 위함이다.
    private ClassificationResult classify(String payload) {
        String trimmed = payload.trim();
        if (trimmed.isEmpty()) {
            return new ClassificationResult("unknown", "empty", 0.0);
        }
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return new ClassificationResult("unknown", "json", 0.9);
        }
        if (trimmed.contains(",")) {
            return new ClassificationResult("unknown", "csv", 0.6);
        }
        return new ClassificationResult("unknown", "text", 0.4);
    }

    public static final class ClassificationResult {
        public final String protocol;
        public final String format;
        public final double confidence;

        public ClassificationResult(String protocol, String format, double confidence) {
            this.protocol = protocol;
            this.format = format;
            this.confidence = confidence;
        }
    }
}
