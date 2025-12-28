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

<<<<<<< Updated upstream
        GatewayRunner runner = new GatewayRunner(parsed);
        int exitCode = runner.run();
        if (exitCode != 0) {
            System.exit(exitCode);
=======
        if (once) {
            // 단일 전송 모드로 업링크를 1회 수행한다.
            // 이유: 시뮬레이터 기반 PR-03에서는 최소 동작 검증이 목표이기 때문이다.
            String payload = loadPayload(inputPath, stdin);
            UplinkResult result = postOnce(url, payload);
            if (result.success) {
                System.out.println("[PASS] gateway uplink " + result.statusCode);
                return;
            } else {
                // 실패 시에는 상태코드와 실패 사유를 함께 출력한다.
                // 이유: 통신 실패 원인을 PASS/FAIL 라인만으로 파악해야 하기 때문이다.
                System.out.println("[FAIL] gateway uplink STATUS=" + result.statusCode
                        + " REASON=" + result.reason);
                System.exit(1);
            }
        }
    }

    private static UplinkResult postOnce(String url, String payload) {
        // Java 표준 HttpClient로 간단히 전송한다.
        if (url == null || url.isBlank()) {
            // 대상 URL이 없으면 실패로 처리한다.
            // 이유: 통신 대상이 없으면 업링크 검증이 불가능하다.
            return UplinkResult.fail(0, GatewayLogReason.UPLINK_URL_MISSING);
        }
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/octet-stream")
                .POST(HttpRequest.BodyPublishers.ofString(payload == null ? "" : payload))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status == 201) {
                return UplinkResult.success(status);
            }
            return UplinkResult.fail(status, GatewayLogReason.UPLINK_BAD_STATUS);
        } catch (IOException | InterruptedException ex) {
            // 통신 실패는 0으로 반환해 스모크에서 실패로 처리한다.
            return UplinkResult.fail(0, GatewayLogReason.UPLINK_SEND_ERROR);
        }
    }

    private static String loadPayload(String inputPath, boolean stdin) {
        if (stdin) {
            return readStdin();
        }
        if (inputPath != null && !inputPath.isBlank()) {
            return readFile(inputPath);
        }
        return loadSamplePayload();
    }

    private static String readStdin() {
        try (InputStream in = System.in; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            return out.toString(StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return "";
        }
    }

    private static String readFile(String inputPath) {
        try {
            return Files.readString(Path.of(inputPath), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return "";
        }
    }

    private static String loadSamplePayload() {
        // 리소스에 샘플이 없더라도 동작하도록 빈 문자열을 반환한다.
        // 이유: 파일 유무로 실행이 중단되면 스모크 자동화가 깨질 수 있다.
        try (InputStream in = GatewayApp.class.getResourceAsStream("/sample-uplink.json")) {
            if (in == null) {
                return "";
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return "";
>>>>>>> Stashed changes
        }
    }

    private static class UplinkResult {
        private final boolean success;
        private final int statusCode;
        private final GatewayLogReason reason;

        private UplinkResult(boolean success, int statusCode, GatewayLogReason reason) {
            this.success = success;
            this.statusCode = statusCode;
            this.reason = reason;
        }

        private static UplinkResult success(int statusCode) {
            return new UplinkResult(true, statusCode, GatewayLogReason.NONE);
        }

        private static UplinkResult fail(int statusCode, GatewayLogReason reason) {
            return new UplinkResult(false, statusCode, reason);
        }
    }
}
