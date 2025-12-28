package com.mes.gateway;

import com.mes.common.logging.PassFailLog;
import com.mes.gateway.downlink.DownlinkFlow;
import com.mes.gateway.downlink.DownlinkResult;
import com.mes.gateway.uplink.UplinkFlow;
import com.mes.gateway.uplink.UplinkResult;

public class GatewayRunner {
    private final GatewayArgs args;

    public GatewayRunner(GatewayArgs args) {
        this.args = args;
    }

    // 게이트웨이 실행 흐름을 정의한다.
    // 이유: main 메서드는 입력 검증과 종료 코드만 담당하고, 실행 로직은 별도 클래스로 분리한다.
    public int run() {
        boolean success = true;

        if (args.getMode().includesUplink()) {
            UplinkResult uplink = new UplinkFlow(args).execute();
            if (uplink.isSuccess()) {
                PassFailLog.pass("gateway uplink " + uplink.getStatusCode());
            } else {
                PassFailLog.fail("gateway uplink STATUS=" + uplink.getStatusCode()
                        + " REASON=" + uplink.getReason());
                success = false;
            }
        }

        if (args.getMode().includesDownlink()) {
            DownlinkResult downlink = new DownlinkFlow(args).execute();
            if (downlink.isSkipped()) {
                PassFailLog.skip("gateway downlink REASON=" + downlink.getReason());
            } else if (downlink.isSuccess()) {
                PassFailLog.pass("gateway downlink " + downlink.getStatusCode());
            } else {
                PassFailLog.fail("gateway downlink STATUS=" + downlink.getStatusCode()
                        + " REASON=" + downlink.getReason());
                success = false;
            }
        }

        return success ? 0 : 1;
    }
}
