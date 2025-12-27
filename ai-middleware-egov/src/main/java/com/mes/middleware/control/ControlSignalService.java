package com.mes.middleware.control;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mes.common.logging.PassFailLog;

@Service
public class ControlSignalService {
    // 초보자 설명:
    // - 이 서비스는 "장비로 보낼 제어 신호"를 저장하고 전송한다.
    // - 아직 실제 장비 프로토콜이 없으므로, 전송은 모의 방식으로 처리한다.
    // 제어 신호 원본을 저장할 폴더명(상대 경로).
    // 이유: 장비/프로토콜 미확정 상태에서도 송신 기록을 남겨야 하기 때문이다.
    private static final String CONTROL_DIR = "data/control";

    // 제어 신호를 저장하고 식별자를 반환한다.
    // 이유: 재전송/감사를 위해 원본을 보관해야 하기 때문이다.
    public String store(String payload) throws IOException {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String id = ts + "-" + UUID.randomUUID();
        Path dir = Paths.get(CONTROL_DIR);
        Files.createDirectories(dir);
        Path file = dir.resolve(id + ".ctl");
        Files.writeString(file, payload == null ? "" : payload, StandardCharsets.UTF_8);
        return id;
    }

    // 제어 신호를 송신한다(현재는 모의 송신).
    // 이유: 실제 프로토콜이 확정되기 전에도 최소 송신 흐름을 검증해야 하기 때문이다.
    // 출력: true면 송신 처리 성공, false면 실패로 간주한다.
    public boolean send(String id, String payload) {
        // 현재 단계에서는 실제 장비로 송신하지 않고 "모의 송신"으로 처리한다.
        // 이유: 프로토콜 미확정 상태에서 실제 전송을 가정하면 잘못된 로그가 남을 수 있다.
        PassFailLog.pass("control signal simulated send");
        return true;
    }
}
