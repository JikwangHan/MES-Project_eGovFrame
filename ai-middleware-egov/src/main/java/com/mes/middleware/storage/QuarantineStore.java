package com.mes.middleware.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;

@Service
public class QuarantineStore {
    // 격리 데이터를 저장할 폴더(상대 경로).
    // 이유: 정규화 실패 데이터는 별도 위치에서 관리해야 재처리가 쉽다.
    private static final String QUARANTINE_DIR = "data/quarantine";

    // 목적: 신뢰도 낮은 데이터를 격리 보관한다.
    // 이유: 자동 파싱 실패 시 원본을 안전하게 보관하기 위함이다.
    // 입력: rawId(원본 식별자), payload(원본 문자열).
    // 출력: 없음(파일 저장).
    public void save(String rawId, String payload) {
        try {
            Path dir = Paths.get(QUARANTINE_DIR);
            Files.createDirectories(dir);
            Path file = dir.resolve(rawId + ".raw");
            Files.writeString(file, payload == null ? "" : payload, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            // 격리 실패 시에도 업링크 처리는 유지한다.
        }
    }
}
