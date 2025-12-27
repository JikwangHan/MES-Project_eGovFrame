package com.mes.gateway;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

public class GatewayApp {
    // 초보자 설명:
    // - 실제 제조장비 대신 "가짜 장비" 역할을 하는 간단한 프로그램이다.
    // - 특정 URL로 원본 데이터를 전송해 연결이 되는지 확인한다.
    // 목적: 장비 데이터를 MES로 전송하는 가장 단순한 시뮬레이터를 제공한다.
    // 이유: 실제 장비가 없어도 업링크 흐름을 검증할 수 있어야 한다.
    public static void main(String[] args) {
        // 기본 업링크 URL은 로컬 AI 미들웨어의 원시 수신 API를 가정한다.
        // 이유: 장비/프로토콜이 미확정인 상태에서도 원시 수신 파이프라인을 검증할 수 있어야 한다.
        String url = "http://localhost:18081/api/raw-ingest";
        boolean once = true;
        String inputPath = null;
        boolean stdin = false;

        for (int i = 0; i < args.length; i++) {
            if ("--url".equals(args[i]) && i + 1 < args.length) {
                url = args[i + 1];
            }
            if ("--once".equals(args[i])) {
                once = true;
            }
            if ("--input".equals(args[i]) && i + 1 < args.length) {
                inputPath = args[i + 1];
            }
            if ("--stdin".equals(args[i])) {
                stdin = true;
            }
        }

        if (once) {
            // 단일 전송 모드로 업링크를 1회 수행한다.
            // 이유: 시뮬레이터 기반 PR-03에서는 최소 동작 검증이 목표이기 때문이다.
            String payload = loadPayload(inputPath, stdin);
            int status = postOnce(url, payload);
            if (status == 201) {
                System.out.println("[PASS] gateway uplink 201");
                return;
            } else {
                System.out.println("[FAIL] gateway uplink " + status);
                System.exit(1);
            }
        }
    }

    private static int postOnce(String url, String payload) {
        // Java 표준 HttpClient로 간단히 전송한다.
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/octet-stream")
                .POST(HttpRequest.BodyPublishers.ofString(payload == null ? "" : payload))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode();
        } catch (IOException | InterruptedException ex) {
            // 통신 실패는 0으로 반환해 스모크에서 실패로 처리한다.
            return 0;
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
        }
    }
}
