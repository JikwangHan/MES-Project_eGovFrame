package com.mes.common.logging;

public final class PassFailLog {
    private PassFailLog() {
    }

    public static void pass(String message) {
        line("PASS", message);
    }

    public static void fail(String message) {
        line("FAIL", message);
    }

    public static void skip(String message) {
        line("SKIP", message);
    }

    private static void line(String level, String message) {
        String safe = message == null ? "" : message;
        System.out.println("[" + level + "] " + safe);
    }
}
