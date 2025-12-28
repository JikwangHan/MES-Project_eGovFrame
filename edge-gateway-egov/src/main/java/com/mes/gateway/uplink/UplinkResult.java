package com.mes.gateway.uplink;

public class UplinkResult {
    private final boolean success;
    private final int statusCode;

    public UplinkResult(boolean success, int statusCode) {
        this.success = success;
        this.statusCode = statusCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
