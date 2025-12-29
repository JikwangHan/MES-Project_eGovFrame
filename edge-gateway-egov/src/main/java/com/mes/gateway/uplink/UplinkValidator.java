package com.mes.gateway.uplink;

// 업링크 샘플 계약(초안)에 대한 최소 검증을 담당한다.
// 이유: 정식 계약이 확정되기 전이라도 필수 필드 유무를 확인해 실패 원인을 표준화한다.
public class UplinkValidator {
    public boolean isValid(String payload) {
        if (payload == null || payload.isBlank()) {
            return false;
        }
        String normalized = payload.replace(" ", "");
        // 초안 기준 필수 필드: deviceId, timestamp, status
        // 이유: 장비 식별/시간/상태는 최소 연동 검증에 반드시 필요하다.
        return normalized.contains("\"deviceId\"")
                && normalized.contains("\"timestamp\"")
                && normalized.contains("\"status\"");
    }
}
