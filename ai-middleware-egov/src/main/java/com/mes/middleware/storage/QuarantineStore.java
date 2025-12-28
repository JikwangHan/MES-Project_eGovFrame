package com.mes.middleware.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class QuarantineStore {
    // 초보자 설명:
    // - "격리"는 신뢰도가 낮은 데이터를 따로 보관하는 것이다.
    // - 나중에 사람이 확인하거나 재처리하기 위해 분리해 둔다.
    // 격리 데이터를 저장할 폴더(상대 경로).
    // 이유: 정규화 실패 데이터는 별도 위치에서 관리해야 재처리가 쉽다.
    private static final String QUARANTINE_DIR = "data/quarantine";
    // 목적: 재처리 이력 파일명을 고정한다.
    // 이유: 히스토리를 한 곳에 모아 추적하기 위함이다.
    private static final String REPROCESS_HISTORY = "reprocess-history.jsonl";
    // 목적: JSON 파싱/저장을 단일 인스턴스로 처리한다.
    // 이유: 반복 생성 비용을 줄이고 일관된 파싱을 유지하기 위함이다.
    private static final ObjectMapper JSON = new ObjectMapper();

    // 목적: 신뢰도 낮은 데이터를 격리 보관한다.
    // 이유: 자동 파싱 실패 시 원본을 안전하게 보관하기 위함이다.
    // 입력: rawId(원본 식별자), payload(원본 문자열), reason(격리 사유), summary(요약).
    // 출력: 없음(파일 저장).
    public void save(String rawId, String payload, String reason, String summary) {
        try {
            Path dir = Paths.get(QUARANTINE_DIR);
            Files.createDirectories(dir);
            Path file = dir.resolve(rawId + ".raw");
            String safePayload = payload == null ? "" : payload;
            Files.writeString(file, safePayload, StandardCharsets.UTF_8);
            // 목적: 격리 메타 정보를 저장한다.
            // 이유: 재처리 대상 조회 기준(기간/상태/사유)을 만들기 위함이다.
            QuarantineMeta meta = new QuarantineMeta();
            meta.rawId = rawId;
            meta.reason = safe(reason);
            meta.summary = safe(summary);
            meta.status = "QUARANTINED";
            meta.payloadSize = safePayload.length();
            meta.quarantinedAt = OffsetDateTime.now().toString();
            writeMeta(dir.resolve(rawId + ".meta.json"), meta);
        } catch (IOException ex) {
            // 격리 실패 시에도 업링크 처리는 유지한다.
        }
    }

    // 목적: 격리 원본을 로드한다.
    // 이유: 재처리 요청 시 원본을 다시 파이프라인에 넣어야 하기 때문이다.
    public String loadPayload(String rawId) {
        try {
            Path file = Paths.get(QUARANTINE_DIR).resolve(rawId + ".raw");
            if (!Files.exists(file)) {
                return null;
            }
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return null;
        }
    }

    // 목적: 재처리 결과 이력을 기록한다.
    // 이유: 재처리 결과를 시간 순서로 추적하기 위함이다.
    public void appendReprocessHistory(String rawId, String result, String summary) {
        try {
            Path dir = Paths.get(QUARANTINE_DIR);
            Files.createDirectories(dir);
            Path history = dir.resolve(REPROCESS_HISTORY);
            String json = "{"
                    + "\"rawId\":\"" + safe(rawId) + "\","
                    + "\"result\":\"" + safe(result) + "\","
                    + "\"summary\":\"" + safe(summary) + "\","
                    + "\"reprocessedAt\":\"" + OffsetDateTime.now().toString() + "\""
                    + "}";
            Files.write(history, List.of(json), StandardCharsets.UTF_8,
                    Files.exists(history) ? java.nio.file.StandardOpenOption.APPEND
                                           : java.nio.file.StandardOpenOption.CREATE);
        } catch (IOException ex) {
            // 이력 저장 실패 시에도 재처리는 계속 진행한다.
        }
    }

    // 목적: 재처리 결과를 메타 파일에 반영한다.
    // 이유: 상태/사유 기반 조회가 가능해야 하기 때문이다.
    public void updateReprocessStatus(String rawId, String result, String summary) {
        try {
            Path metaPath = Paths.get(QUARANTINE_DIR).resolve(rawId + ".meta.json");
            QuarantineMeta meta = readMeta(metaPath);
            if (meta == null) {
                meta = new QuarantineMeta();
                meta.rawId = rawId;
                meta.quarantinedAt = OffsetDateTime.now().toString();
            }
            meta.status = "REPROCESSED";
            meta.lastResult = safe(result);
            meta.lastSummary = safe(summary);
            meta.lastReprocessedAt = OffsetDateTime.now().toString();
            writeMeta(metaPath, meta);
        } catch (IOException ex) {
            // 메타 갱신 실패 시에도 재처리는 계속 진행한다.
        }
    }

    // 목적: 재처리 대상 조회 기준으로 후보를 걸러낸다.
    // 이유: 기간/상태/사유 기준을 최소 수준으로 제공해야 하기 때문이다.
    public List<QuarantineMeta> listCandidates(String from, String to, String status, String reason) {
        Path dir = Paths.get(QUARANTINE_DIR);
        if (!Files.exists(dir)) {
            return List.of();
        }
        List<Path> files;
        try {
            files = Files.list(dir)
                    .filter(p -> p.getFileName().toString().endsWith(".meta.json"))
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            return List.of();
        }

        LocalDate fromDate = parseDate(from);
        LocalDate toDate = parseDate(to);
        List<QuarantineMeta> result = new ArrayList<>();
        for (Path metaPath : files) {
            QuarantineMeta meta = readMeta(metaPath);
            if (meta == null) {
                continue;
            }
            if (!matchDate(meta.quarantinedAt, fromDate, toDate)) {
                continue;
            }
            if (!matchContains(meta.status, status)) {
                continue;
            }
            if (!matchContains(meta.reason, reason)) {
                continue;
            }
            result.add(meta);
        }
        return result;
    }

    private boolean matchContains(String value, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String left = value == null ? "" : value.toLowerCase(Locale.ROOT);
        String right = keyword.toLowerCase(Locale.ROOT);
        return left.contains(right);
    }

    private boolean matchDate(String isoTime, LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return true;
        }
        try {
            OffsetDateTime time = OffsetDateTime.parse(isoTime);
            LocalDate date = time.withOffsetSameInstant(ZoneOffset.UTC).toLocalDate();
            if (from != null && date.isBefore(from)) {
                return false;
            }
            if (to != null && date.isAfter(to)) {
                return false;
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private QuarantineMeta readMeta(Path metaPath) {
        if (metaPath == null || !Files.exists(metaPath)) {
            return null;
        }
        try {
            return JSON.readValue(metaPath.toFile(), QuarantineMeta.class);
        } catch (IOException ex) {
            return null;
        }
    }

    private void writeMeta(Path metaPath, QuarantineMeta meta) throws IOException {
        JSON.writerWithDefaultPrettyPrinter().writeValue(metaPath.toFile(), meta);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    // 목적: 격리 메타 정보를 한 곳에 모아두는 구조다.
    // 이유: 재처리 대상 조회와 결과 기록을 단순화하기 위함이다.
    public static final class QuarantineMeta {
        public String rawId;
        public String reason;
        public String summary;
        public String status;
        public int payloadSize;
        public String quarantinedAt;
        public String lastResult;
        public String lastSummary;
        public String lastReprocessedAt;
    }
}
