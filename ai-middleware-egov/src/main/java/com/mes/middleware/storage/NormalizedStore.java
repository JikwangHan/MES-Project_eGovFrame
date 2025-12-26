package com.mes.middleware.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mes.middleware.pipeline.RawPipelineService.ClassificationResult;

@Service
public class NormalizedStore {
    private static final String NORMALIZED_DIR = "data/normalized";

    // 목적: 분류 결과를 최소 정규화 형태로 보관한다.
    // 이유: 후속 단계에서 DB 적재 경로를 대체하기 위함이다.
    public void save(String rawId, ClassificationResult result) {
        try {
            Path dir = Paths.get(NORMALIZED_DIR);
            Files.createDirectories(dir);
            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            Path file = dir.resolve(ts + "-" + rawId + ".json");
            String json = buildJson(rawId, result);
            Files.writeString(file, json, StandardCharsets.UTF_8);
            // 누적 파일에 한 줄씩 저장해 후속 분석에 사용한다.
            Path aggregated = dir.resolve("normalized.jsonl");
            Files.write(aggregated, List.of(json), StandardCharsets.UTF_8,
                    Files.exists(aggregated) ? java.nio.file.StandardOpenOption.APPEND
                                             : java.nio.file.StandardOpenOption.CREATE);
        } catch (IOException ex) {
            // 정규화 저장 실패 시에도 업링크 처리는 유지한다.
        }
    }

    private String buildJson(String rawId, ClassificationResult result) {
        return "{"
            + "\"rawId\":\"" + escape(rawId) + "\","
            + "\"protocol\":\"" + escape(result.protocol) + "\","
            + "\"format\":\"" + escape(result.format) + "\","
            + "\"confidence\":" + result.confidence
            + "}";
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
