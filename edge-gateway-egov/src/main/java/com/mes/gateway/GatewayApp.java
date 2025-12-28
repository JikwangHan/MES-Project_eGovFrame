package com.mes.gateway;

import com.mes.common.logging.PassFailLog;

public class GatewayApp {
    // 초보자 설명:
    // - 이 프로그램은 실제 제조장비가 없을 때도 업링크/다운링크 흐름을 검증하는 게이트웨이 시뮬레이터이다.
    // - PR-A1 단계에서는 구조(스캐폴딩)를 먼저 잡고, 실제 프로토콜 연동은 이후 단계로 미룬다.
    // 이유: 초기에는 흐름과 실패 처리 기준을 고정해두어야 이후 확장 시 오류를 줄일 수 있다.
    public static void main(String[] args) {
        GatewayArgs parsed = GatewayArgs.parse(args);
        if (!parsed.isValid()) {
            // 입력 인자 오류는 즉시 FAIL로 종료한다.
            // 이유: 잘못된 입력을 묵인하면 이후 단계에서 원인 추적이 어려워진다.
            PassFailLog.fail("gateway args invalid");
            System.exit(1);
        }

        GatewayRunner runner = new GatewayRunner(parsed);
        int exitCode = runner.run();
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}
