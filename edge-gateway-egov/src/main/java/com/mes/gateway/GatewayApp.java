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
            return 0;
        }
    }

    private static String loadSamplePayload() {
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
