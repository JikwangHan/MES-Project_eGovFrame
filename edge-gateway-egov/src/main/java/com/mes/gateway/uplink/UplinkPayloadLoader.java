package com.mes.gateway.uplink;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class UplinkPayloadLoader {
    // 입력 소스를 기준으로 업링크 원본 데이터를 준비한다.
    // 이유: 입력 경로/표준입력/샘플 로딩을 한 곳에서 관리하면 흐름이 명확해진다.
    public String load(String inputPath, boolean stdin) {
        if (stdin) {
            return readStdin();
        }
        if (inputPath != null && !inputPath.isBlank()) {
            return readFile(inputPath);
        }
        return loadSamplePayload();
    }

    private String readStdin() {
        try (InputStream in = System.in; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            return out.toString(StandardCharsets.UTF_8);
        } catch (IOException ex) {
            // 표준 입력이 실패하면 빈 문자열로 처리한다.
            // 이유: 스모크 자동화에서는 입력 실패가 전체 종료로 이어지면 안 된다.
            return "";
        }
    }

    private String readFile(String inputPath) {
        try {
            return Files.readString(Path.of(inputPath), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            // 파일 읽기 실패 시 빈 문자열로 처리한다.
            // 이유: 파일 경로가 잘못되어도 기본 흐름을 검증할 수 있어야 한다.
            return "";
        }
    }

    private String loadSamplePayload() {
        // 리소스에 샘플이 없더라도 동작하도록 빈 문자열을 반환한다.
        // 이유: 샘플 파일 유무로 실행이 중단되면 스모크 자동화가 깨질 수 있다.
        try (InputStream in = UplinkPayloadLoader.class.getResourceAsStream("/sample-uplink.json")) {
            if (in == null) {
                return "";
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return "";
        }
    }
}
