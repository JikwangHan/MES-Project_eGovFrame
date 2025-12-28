package com.mes.gateway.downlink;

import com.mes.gateway.GatewayLogReason;

public class DownlinkResult {
    private final boolean success;
    private final boolean skipped;
    private final int statusCode;
    private final GatewayLogReason reason;

    public DownlinkResult(boolean success, boolean skipped, int statusCode, GatewayLogReason reason) {
        this.success = success;
        this.skipped = skipped;
        this.statusCode = statusCode;
        this.reason = reason;
    }

    public static DownlinkResult success(int statusCode) {
        return new DownlinkResult(true, false, statusCode, GatewayLogReason.NONE);
    }

    public static DownlinkResult skip(GatewayLogReason reason) {
        return new DownlinkResult(false, true, 0, reason);
    }

    public static DownlinkResult fail(int statusCode, GatewayLogReason reason) {
        return new DownlinkResult(false, false, statusCode, reason);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isSkipped() {
        return skipped;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public GatewayLogReason getReason() {
        return reason;
    }
}
