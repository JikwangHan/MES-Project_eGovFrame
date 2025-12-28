package com.mes.middleware.pipeline;

import org.springframework.stereotype.Service;

import com.mes.middleware.storage.QuarantineStore;

@Service
public class QuarantineReprocessService {
    // 목적: 격리 저장소에 접근해 재처리 입력을 꺼내기 위함이다.
    // 이유: 원본을 다시 파이프라인에 넣어 재처리하기 위해서다.
    private final QuarantineStore quarantineStore;
    // 목적: 기존 파이프라인을 재처리에 재사용한다.
    // 이유: 새로운 로직을 만들면 검증 기준이 달라져 일관성이 깨질 수 있다.
    private final RawPipelineService rawPipelineService;

    public QuarantineReprocessService(QuarantineStore quarantineStore, RawPipelineService rawPipelineService) {
        this.quarantineStore = quarantineStore;
        this.rawPipelineService = rawPipelineService;
    }

    // 목적: 격리 데이터 1건을 재처리한다.
    // 이유: 재처리 결과를 기록해 운영자가 추적할 수 있어야 하기 때문이다.
    public ReprocessResult reprocess(String rawId) {
        if (rawId == null || rawId.isBlank()) {
            return ReprocessResult.fail("EMPTY_RAW_ID", "rawId required");
        }
        String payload = quarantineStore.loadPayload(rawId);
        if (payload == null) {
            return ReprocessResult.fail("NOT_FOUND", "payload not found");
        }
        RawPipelineService.ValidationResult result = rawPipelineService.process(rawId, payload);
        quarantineStore.appendReprocessHistory(rawId, result.decision.name(), result.summary);
        quarantineStore.updateReprocessStatus(rawId, result.decision.name(), result.summary);
        return ReprocessResult.ok(result.decision.name(), result.reason, result.summary);
    }

    // 목적: 재처리 결과를 호출자에게 전달한다.
    // 이유: API 응답과 이력 기록에 동일한 정보를 쓰기 위함이다.
    public static final class ReprocessResult {
        public final boolean success;
        public final String code;
        public final String decision;
        public final String reason;
        public final String summary;

        private ReprocessResult(boolean success, String code, String decision, String reason, String summary) {
            this.success = success;
            this.code = code;
            this.decision = decision;
            this.reason = reason;
            this.summary = summary;
        }

        private static ReprocessResult ok(String decision, String reason, String summary) {
            return new ReprocessResult(true, "OK", decision, reason, summary);
        }

        private static ReprocessResult fail(String code, String message) {
            return new ReprocessResult(false, code, "NONE", message, "");
        }
    }
}
