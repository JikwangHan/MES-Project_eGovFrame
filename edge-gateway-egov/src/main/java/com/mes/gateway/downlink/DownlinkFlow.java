package com.mes.gateway.downlink;

import com.mes.gateway.GatewayArgs;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public class DownlinkFlow {
    private final GatewayArgs args;

    public DownlinkFlow(GatewayArgs args) {
        this.args = args;
    }

    // 다운링크 기본 흐름: 명령 조회 -> (옵션) 파일 저장.
    // 이유: 다운링크는 장비 제어와 연결되므로 최소 구조만 먼저 고정한다.
    public DownlinkResult execute() {
        String url = args.getDownlinkUrl();
        if (url == null || url.isBlank()) {
            // 다운링크 URL이 없으면 스킵으로 처리한다.
            // 이유: PR-A1에서는 다운링크 계약이 확정되지 않았기 때문이다.
            return DownlinkResult.skip();
        }

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(args.getTimeoutSeconds()))
                .build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(args.getTimeoutSeconds()))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status == 204) {
                // 명령이 없는 경우는 스킵 처리한다.
                return DownlinkResult.skip();
            }
            if (status == 200) {
                if (args.getDownlinkOutputPath() != null && !args.getDownlinkOutputPath().isBlank()) {
                    boolean stored = writeOutput(args.getDownlinkOutputPath(), response.body());
                    if (!stored) {
                        return DownlinkResult.fail(status);
                    }
                }
                return DownlinkResult.success(status);
            }
            return DownlinkResult.fail(status);
        } catch (IOException | InterruptedException ex) {
            return DownlinkResult.fail(0);
        }
    }

    private boolean writeOutput(String outputPath, String payload) {
        try {
            Path target = Path.of(outputPath);
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(target, payload == null ? "" : payload, StandardCharsets.UTF_8);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
}
