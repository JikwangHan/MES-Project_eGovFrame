package com.mes.middleware.control;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mes.common.logging.PassFailLog;

@RestController
public class ControlSignalController {
    // 초보자 설명:
    // - 장비 제어 요청을 받는 API이다.
    // - 실제 장비 제어 대신 "저장 + 모의 송신"으로 흐름을 검증한다.
    private final ControlSignalService controlSignalService;

    public ControlSignalController(ControlSignalService controlSignalService) {
        // 제어 신호 저장/송신 흐름을 서비스로 위임한다.
        this.controlSignalService = controlSignalService;
    }

    // 제어 신호 송신 API.
    // 목적: 장비/프로토콜 미확정 상태에서도 제어 요청 흐름을 검증한다.
    // 입력: 제어 신호 원본(payload) 문자열.
    // 출력: 저장된 제어 신호 식별자.
    @PostMapping("/api/control-signal")
    public ResponseEntity<Map<String, Object>> send(@RequestBody String payload) {
        // 초보자 설명:
        // - 요청을 받으면 먼저 파일로 저장하고,
        // - 그 다음 모의 전송을 수행한다.
        try {
            // 1) 제어 신호 원본을 저장한다.
            String id = controlSignalService.store(payload);
            // 2) 현재는 모의 송신으로 처리한다.
            boolean sent = controlSignalService.send(id, payload);
            if (!sent) {
                PassFailLog.fail("control signal send failed");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(fail("E-2001", "control send failed"));
            }
            // 3) 정상 처리 결과를 표준 응답으로 반환한다.
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", id);
            return ResponseEntity.status(HttpStatus.CREATED).body(ok(data));
        } catch (IOException ex) {
            PassFailLog.fail("control signal store failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(fail("E-2000", "control store failed"));
        }
    }

    // 공통 성공 응답 포맷.
    private Map<String, Object> ok(Object data) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("result", "OK");
        res.put("message", "");
        res.put("data", data);
        return res;
    }

    // 공통 실패 응답 포맷.
    private Map<String, Object> fail(String errorCode, String message) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("result", "FAIL");
        res.put("message", message);
        res.put("errorCode", errorCode);
        res.put("data", null);
        return res;
    }
}
