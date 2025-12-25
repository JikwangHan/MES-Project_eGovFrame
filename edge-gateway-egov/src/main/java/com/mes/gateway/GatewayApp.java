package com.mes.gateway;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class GatewayApp {
    public static void main(String[] args) {
        // 기본 업링크 URL은 로컬 MES Web을 가정한다.
        // 이유: 초기 스모크는 로컬에서 빠르게 반복 검증하는 것이 가장 안전하다.
        String url = "http://localhost:18080/api/uplink";
        boolean once = true;

        for (int i = 0; i < args.length; i++) {
            if ("--url".equals(args[i]) && i + 1 < args.length) {
                url = args[i + 1];
            }
            if ("--once".equals(args[i])) {
                once = true;
            }
        }

        if (once) {
            // 단일 전송 모드로 업링크를 1회 수행한다.
            // 이유: 시뮬레이터 기반 PR-03에서는 최소 동작 검증이 목표이기 때문이다.
            int status = postOnce(url);
            if (status == 201) {
                System.out.println("[PASS] gateway uplink 201");
                return;
            } else {
                System.out.println("[FAIL] gateway uplink " + status);
                System.exit(1);
            }
        }
    }

    private static int postOnce(String url) {
        // 샘플 JSON을 읽어 업링크 바디로 사용한다.
        // 이유: 데이터 포맷이 아직 고정되지 않았으므로, 간단한 표준 구조만 유지한다.
        String payload = loadSamplePayload();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode();
        } catch (IOException | InterruptedException ex) {
            // 통신 실패는 0으로 반환해 스모크에서 실패로 처리한다.
            return 0;
        }
    }

    private static String loadSamplePayload() {
        // 리소스에 샘플이 없더라도 동작하도록 {}를 반환한다.
        // 이유: 파일 유무로 실행이 중단되면 스모크 자동화가 깨질 수 있다.
        try (InputStream in = GatewayApp.class.getResourceAsStream("/sample-uplink.json")) {
            if (in == null) {
                return "{}";
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return "{}";
        }
    }
}
