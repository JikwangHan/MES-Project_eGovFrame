package com.mes.gateway.downlink;

public class DownlinkResult {
    private final boolean success;
    private final boolean skipped;
    private final int statusCode;

    public DownlinkResult(boolean success, boolean skipped, int statusCode) {
        this.success = success;
        this.skipped = skipped;
        this.statusCode = statusCode;
    }

    public static DownlinkResult success(int statusCode) {
        return new DownlinkResult(true, false, statusCode);
    }

    public static DownlinkResult skip() {
        return new DownlinkResult(false, true, 0);
    }

    public static DownlinkResult fail(int statusCode) {
        return new DownlinkResult(false, false, statusCode);
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
}
