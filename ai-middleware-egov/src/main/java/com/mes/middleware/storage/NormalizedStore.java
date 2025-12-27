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

@Service
public class NormalizedStore {
    // 초보자 설명:
    // - "정규화"란 제각각인 원본 데이터를 공통 형태로 바꾸는 과정이다.
    // - 아직 DB가 없으므로 파일에 저장해 결과를 확인한다.
    // 정규화 데이터를 저장할 폴더(상대 경로).
    // 이유: DB 이전 단계에서 파일 기반으로 먼저 결과를 확인하기 위함이다.
    private static final String NORMALIZED_DIR = "data/normalized";

    // 목적: 정규화 결과를 최소 정규화 형태로 보관한다.
    // 이유: 후속 단계에서 DB 적재 경로를 대체하기 위함이다.
    // 입력: record(정규화 레코드).
    // 출력: 없음(파일 저장).
    public void save(NormalizedRecord record) {
        try {
            Path dir = Paths.get(NORMALIZED_DIR);
            Files.createDirectories(dir);
            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            Path file = dir.resolve(ts + "-" + record.rawId + ".json");
            String json = buildJson(record);
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

    // 목적: 분류 결과를 JSON 문자열로 변환한다.
    // 이유: 파일 기반 저장은 JSON이 가장 다루기 쉽기 때문이다.
    private String buildJson(NormalizedRecord record) {
        return "{"
            + "\"rawId\":\"" + escape(record.rawId) + "\","
            + "\"deviceHint\":\"" + escape(record.deviceHint) + "\","
            + "\"protocolHint\":\"" + escape(record.protocolHint) + "\","
            + "\"format\":\"" + escape(record.format) + "\","
            + "\"eventType\":\"" + escape(record.eventType) + "\","
            + "\"eventTime\":\"" + escape(record.eventTime) + "\","
            + "\"payloadJson\":" + safeJson(record.payloadJson) + ","
            + "\"confidence\":" + record.confidence + ","
            + "\"validationStatus\":\"" + escape(record.validationStatus) + "\","
            + "\"createdAt\":\"" + escape(record.createdAt) + "\""
            + "}";
    }

    // 목적: JSON 문자열에 안전하게 들어가도록 이스케이프 처리한다.
    // 이유: 따옴표/역슬래시가 포함되면 JSON이 깨질 수 있다.
    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // 목적: 이미 JSON 문자열인 payloadJson을 안전하게 본문에 삽입한다.
    // 이유: 이중 따옴표를 중복 처리하면 JSON이 깨질 수 있다.
    private String safeJson(String value) {
        if (value == null || value.isBlank()) {
            return "{}";
        }
        return value.trim();
    }

    // 목적: 정규화 레코드 구조를 명확히 정의한다.
    // 이유: 저장 규칙을 문서/코드에서 동시에 고정하기 위함이다.
    public static final class NormalizedRecord {
        public String rawId;
        public String deviceHint;
        public String protocolHint;
        public String format;
        public String eventType;
        public String eventTime;
        public String payloadJson;
        public double confidence;
        public String validationStatus;
        public String createdAt;
    }
}
