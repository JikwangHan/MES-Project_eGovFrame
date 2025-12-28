package com.mes.gateway;

public enum GatewayMode {
    // 업링크만 수행한다.
    UPLINK,
    // 다운링크만 수행한다.
    DOWNLINK,
    // 업링크와 다운링크를 순서대로 수행한다.
    BOTH;

    // 현재 모드가 업링크를 포함하는지 판단한다.
    // 이유: 모드별 분기를 한 곳에서 통일해 유지보수를 쉽게 한다.
    public boolean includesUplink() {
        return this == UPLINK || this == BOTH;
    }

    // 현재 모드가 다운링크를 포함하는지 판단한다.
    // 이유: 모드 분기가 흩어지면 추후 변경 시 오류가 발생하기 쉽다.
    public boolean includesDownlink() {
        return this == DOWNLINK || this == BOTH;
    }

    // 문자열 입력을 안전하게 모드로 변환한다.
    // 이유: 사용자가 잘못된 입력을 넣더라도 예외 대신 FAIL 처리로 일관한다.
    public static GatewayMode from(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim().toLowerCase();
        switch (normalized) {
            case "uplink":
                return UPLINK;
            case "downlink":
                return DOWNLINK;
            case "both":
                return BOTH;
            default:
                return null;
        }
    }
}
