package com.mes.gateway.uplink;

import com.mes.gateway.GatewayLogReason;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class UplinkSender {
    // 업링크 전송을 담당한다.
    // 이유: 전송 로직을 분리하면 재전송/백오프 같은 확장 기능을 쉽게 추가할 수 있다.
    public UplinkResult send(String url, String payload, int timeoutSeconds) {
        if (url == null || url.isBlank()) {
            // 업링크 목적지가 없으면 실패로 처리한다.
            // 이유: 목적지가 없으면 업링크 결과를 검증할 수 없기 때문이다.
            return UplinkResult.fail(0, GatewayLogReason.UPLINK_URL_MISSING);
        }

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(timeoutSeconds))
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
            // 통신 실패는 statusCode 0으로 표준화한다.
            // 이유: 스모크 스크립트가 실패를 명확히 인지할 수 있어야 한다.
            return UplinkResult.fail(0, GatewayLogReason.UPLINK_SEND_ERROR);
        }
    }
}
