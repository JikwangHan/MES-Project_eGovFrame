package com.mes.gateway;

public class GatewayArgs {
    // 기본 업링크 URL은 로컬 AI 미들웨어의 원시 수신 API를 가정한다.
    // 이유: 프로토콜이 확정되지 않은 단계에서도 원시 수신 파이프라인을 검증할 수 있어야 한다.
    private String uplinkUrl = "http://localhost:18081/api/raw-ingest";
    private String downlinkUrl;
    private String inputPath;
    private boolean stdin;
    private GatewayMode mode = GatewayMode.UPLINK;
    private int timeoutSeconds = 5;
    private String downlinkOutputPath;
    private boolean once = true;
    private boolean valid = true;

    public static GatewayArgs parse(String[] args) {
        GatewayArgs parsed = new GatewayArgs();
        if (args == null) {
            return parsed;
        }

        for (int i = 0; i < args.length; i++) {
            String current = args[i];
            if ("--url".equals(current) && i + 1 < args.length) {
                parsed.uplinkUrl = args[++i];
                continue;
            }
            if ("--uplink-url".equals(current) && i + 1 < args.length) {
                parsed.uplinkUrl = args[++i];
                continue;
            }
            if ("--downlink-url".equals(current) && i + 1 < args.length) {
                parsed.downlinkUrl = args[++i];
                continue;
            }
            if ("--input".equals(current) && i + 1 < args.length) {
                parsed.inputPath = args[++i];
                continue;
            }
            if ("--stdin".equals(current)) {
                parsed.stdin = true;
                continue;
            }
            if ("--mode".equals(current) && i + 1 < args.length) {
                GatewayMode parsedMode = GatewayMode.from(args[++i]);
                if (parsedMode == null) {
                    parsed.valid = false;
                } else {
                    parsed.mode = parsedMode;
                }
                continue;
            }
            if ("--timeout".equals(current) && i + 1 < args.length) {
                parsed.timeoutSeconds = parsePositiveInt(args[++i]);
                if (parsed.timeoutSeconds <= 0) {
                    parsed.valid = false;
                }
                continue;
            }
            if ("--downlink-output".equals(current) && i + 1 < args.length) {
                parsed.downlinkOutputPath = args[++i];
                continue;
            }
            if ("--once".equals(current)) {
                parsed.once = true;
                continue;
            }

            // 알 수 없는 입력은 잘못된 사용으로 간주한다.
            // 이유: 모르는 옵션을 조용히 무시하면 실행 결과가 예측 불가능해진다.
            parsed.valid = false;
        }

        return parsed;
    }

    private static int parsePositiveInt(String raw) {
        try {
            int value = Integer.parseInt(raw);
            return value <= 0 ? -1 : value;
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    public boolean isValid() {
        return valid;
    }

    public String getUplinkUrl() {
        return uplinkUrl;
    }

    public String getDownlinkUrl() {
        return downlinkUrl;
    }

    public String getInputPath() {
        return inputPath;
    }

    public boolean isStdin() {
        return stdin;
    }

    public GatewayMode getMode() {
        return mode;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public String getDownlinkOutputPath() {
        return downlinkOutputPath;
    }

    public boolean isOnce() {
        return once;
    }
}
