package com.mes.gateway.uplink;

import com.mes.gateway.GatewayArgs;

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
        return new UplinkSender().send(args.getUplinkUrl(), normalizedPayload, args.getTimeoutSeconds());
    }
}
