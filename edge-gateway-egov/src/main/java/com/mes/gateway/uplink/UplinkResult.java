package com.mes.gateway.uplink;

import com.mes.gateway.GatewayLogReason;

public class UplinkResult {
    private final boolean success;
    private final int statusCode;
    private final GatewayLogReason reason;

    public UplinkResult(boolean success, int statusCode, GatewayLogReason reason) {
        this.success = success;
        this.statusCode = statusCode;
        this.reason = reason;
    }

    public static UplinkResult success(int statusCode) {
        return new UplinkResult(true, statusCode, GatewayLogReason.NONE);
    }

    public static UplinkResult fail(int statusCode, GatewayLogReason reason) {
        return new UplinkResult(false, statusCode, reason);
    }

    public boolean isSuccess() {
        return success;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public GatewayLogReason getReason() {
        return reason;
    }
}
