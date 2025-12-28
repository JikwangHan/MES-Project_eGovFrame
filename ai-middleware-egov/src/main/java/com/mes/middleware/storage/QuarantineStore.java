package com.mes.middleware.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;

@Service
public class QuarantineStore {
    // 초보자 설명:
    // - "격리"는 신뢰도가 낮은 데이터를 따로 보관하는 것이다.
    // - 나중에 사람이 확인하거나 재처리하기 위해 분리해 둔다.
    // 격리 데이터를 저장할 폴더(상대 경로).
    // 이유: 정규화 실패 데이터는 별도 위치에서 관리해야 재처리가 쉽다.
    private static final String QUARANTINE_DIR = "data/quarantine";

    // 목적: 신뢰도 낮은 데이터를 격리 보관한다.
    // 이유: 자동 파싱 실패 시 원본을 안전하게 보관하기 위함이다.
    // 입력: rawId(원본 식별자), payload(원본 문자열).
    // 출력: 없음(파일 저장).
<<<<<<< Updated upstream
<<<<<<< Updated upstream
    public void save(String rawId, String payload, String reason) {
=======
    public void save(String rawId, String payload, String reason, String summary) {
>>>>>>> Stashed changes
=======
    public void save(String rawId, String payload, String reason, String summary) {
>>>>>>> Stashed changes
        try {
            Path dir = Paths.get(QUARANTINE_DIR);
            Files.createDirectories(dir);
            Path file = dir.resolve(rawId + ".raw");
<<<<<<< Updated upstream
<<<<<<< Updated upstream
            Files.writeString(file, payload == null ? "" : payload, StandardCharsets.UTF_8);
            // 목적: 격리 사유를 메타 파일로 보관한다.
            // 이유: 사유가 있어야 후속 재처리 기준을 빠르게 합의할 수 있다.
            Path meta = dir.resolve(rawId + ".meta.json");
            String safeReason = reason == null ? "" : reason.replace("\"", "\\\"");
            String json = "{"
                    + "\"rawId\":\"" + rawId + "\","
                    + "\"reason\":\"" + safeReason + "\","
=======
=======
>>>>>>> Stashed changes
            String safePayload = payload == null ? "" : payload;
            Files.writeString(file, safePayload, StandardCharsets.UTF_8);
            // 목적: 격리 사유/요약/시간을 메타 파일로 보관한다.
            // 이유: 재처리 기준을 빠르게 합의할 수 있도록 최소 정보를 남기기 위함이다.
            Path meta = dir.resolve(rawId + ".meta.json");
            String safeReason = escape(reason);
            String safeSummary = escape(summary);
            int payloadSize = safePayload.length();
            String json = "{"
                    + "\"rawId\":\"" + rawId + "\","
                    + "\"reason\":\"" + safeReason + "\","
                    + "\"summary\":\"" + safeSummary + "\","
                    + "\"payloadSize\":" + payloadSize + ","
<<<<<<< Updated upstream
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
                    + "\"quarantinedAt\":\"" + OffsetDateTime.now().toString() + "\""
                    + "}";
            Files.writeString(meta, json, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            // 격리 실패 시에도 업링크 처리는 유지한다.
        }
    }

    // 목적: 메타 JSON에 안전하게 들어가도록 이스케이프 처리한다.
    // 이유: 따옴표/역슬래시가 포함되면 JSON이 깨질 수 있기 때문이다.
    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
