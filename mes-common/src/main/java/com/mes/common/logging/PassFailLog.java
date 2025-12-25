package com.mes.common.logging;

public final class PassFailLog {
    // PASS/FAIL/SKIP 표준 라인만 출력하기 위해 단일 진입점을 둔다.
    // 이유: 운영 증빙과 자동화 스크립트는 특정 문자열 형식에 의존하므로, 출력 규칙을 한 곳에서 고정한다.
    private PassFailLog() {
    }

    // 성공 근거를 표준 형식으로 출력한다. 운영 증빙을 위해 형식을 고정한다.
    public static void pass(String message) {
        line("PASS", message);
    }

    // 실패 근거를 표준 형식으로 출력한다. 민감정보는 message에 포함하지 않는다.
    public static void fail(String message) {
        line("FAIL", message);
    }

    // 스킵 근거를 표준 형식으로 출력한다. 조건 미충족 시 일관된 결과를 남긴다.
    public static void skip(String message) {
        line("SKIP", message);
    }

    // 로그 포맷을 강제하여 운영 증빙이 항상 같은 규격이 되도록 한다.
    // message가 null인 경우에도 예외가 나지 않도록 빈 문자열로 처리한다.
    private static void line(String level, String message) {
        String safe = message == null ? "" : message;
        System.out.println("[" + level + "] " + safe);
    }
}
