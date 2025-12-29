package com.mes.middleware.pipeline;

import org.springframework.stereotype.Service;

import com.mes.middleware.storage.QuarantineStore;

@Service
public class QuarantineDecisionService {
    // 목적: 승인/거부 결과를 저장소에 반영하기 위함이다.
    // 이유: 승인/거부는 격리 처리의 마지막 단계이므로 일관된 기록이 필요하다.
    private final QuarantineStore quarantineStore;

    public QuarantineDecisionService(QuarantineStore quarantineStore) {
        this.quarantineStore = quarantineStore;
    }

    // 목적: 격리 데이터에 대한 승인/거부 결정을 기록한다.
    // 이유: 결과 통계와 운영 추적을 위해 결정 정보를 남겨야 하기 때문이다.
    public DecisionResult decide(String rawId, String decision, String summary) {
        if (rawId == null || rawId.isBlank()) {
            return DecisionResult.fail("EMPTY_RAW_ID", "rawId required");
        }
        if (decision == null || decision.isBlank()) {
            return DecisionResult.fail("EMPTY_DECISION", "decision required");
        }
        String normalized = decision.trim().toUpperCase();
        if (!"APPROVED".equals(normalized) && !"REJECTED".equals(normalized)) {
            return DecisionResult.fail("INVALID_DECISION", "decision must be APPROVED or REJECTED");
        }
        String safeSummary = summary == null ? "" : summary;
        quarantineStore.updateDecisionStatus(rawId, normalized, safeSummary);
        quarantineStore.appendDecisionHistory(rawId, normalized, safeSummary);
        return DecisionResult.ok(normalized, safeSummary);
    }

    // 목적: 승인/거부 처리 결과를 전달한다.
    // 이유: API 응답과 운영 이력에 동일 기준을 사용하기 위함이다.
    public static final class DecisionResult {
        public final boolean success;
        public final String code;
        public final String decision;
        public final String summary;

        private DecisionResult(boolean success, String code, String decision, String summary) {
            this.success = success;
            this.code = code;
            this.decision = decision;
            this.summary = summary;
        }

        private static DecisionResult ok(String decision, String summary) {
            return new DecisionResult(true, "OK", decision, summary);
        }

        private static DecisionResult fail(String code, String summary) {
            return new DecisionResult(false, code, "NONE", summary);
        }
    }
}
