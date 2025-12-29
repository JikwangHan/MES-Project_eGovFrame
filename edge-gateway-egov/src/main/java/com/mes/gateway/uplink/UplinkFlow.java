package com.mes.gateway.uplink;

import com.mes.gateway.GatewayArgs;
import com.mes.gateway.GatewayLogReason;

public class UplinkFlow {
    private final GatewayArgs args;

    public UplinkFlow(GatewayArgs args) {
        this.args = args;
    }

    // 업링크 기본 흐름: 로드 -> 정규화 -> 전송.
    // 이유: 단계별 책임을 나누면 향후 프로토콜 추가 시 변경 범위를 줄일 수 있다.
    public UplinkResult execute() {
        String rawPayload = new UplinkPayloadLoader().load(args.getInputPath(), args.isStdin());
        String normalizedPayload = new UplinkNormalizer().normalize(rawPayload);
        boolean valid = new UplinkValidator().isValid(normalizedPayload);
        if (!valid) {
            // 계약(초안) 기준 검증 실패는 전송을 중단한다.
            // 이유: 잘못된 샘플이 업링크되면 이후 단계 검증이 왜곡된다.
            return UplinkResult.fail(0, GatewayLogReason.UPLINK_PAYLOAD_INVALID);
        }
        return new UplinkSender().send(args.getUplinkUrl(), normalizedPayload, args.getTimeoutSeconds());
    }
}
