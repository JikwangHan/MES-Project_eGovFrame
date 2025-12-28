package com.mes.middleware.api;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mes.middleware.pipeline.RawPipelineService;
import com.mes.middleware.storage.RawStore;

@RestController
public class RawIngestController {
    // 초보자 설명:
    // - 장비에서 온 "원본 데이터"를 가장 먼저 받는 API다.
    // - 받은 데이터를 파일로 저장하고, AI 파이프라인으로 넘긴다.
    private final RawStore rawStore;
    private final RawPipelineService pipelineService;

    public RawIngestController(RawStore rawStore, RawPipelineService pipelineService) {
        this.rawStore = rawStore;
        // 파이프라인 서비스는 원본 저장 이후의 자동 분류/정규화 흐름을 담당한다.
        this.pipelineService = pipelineService;
    }

    // 원본 수신 엔드포인트.
    // 목적: 어떤 장비/프로토콜인지 모르는 원시 데이터를 먼저 수신한다.
    // 이유: 이후 단계에서 AI 분류/정규화를 수행하기 위해 원본을 반드시 확보해야 한다.
    // 입력: raw payload 문자열.
    // 출력: 저장된 raw 데이터의 식별자.
    @PostMapping("/api/raw-ingest")
    public ResponseEntity<Map<String, Object>> rawIngest(@RequestBody String payload) {
        try {
            // 1) 원본 데이터를 파일로 보관한다.
            String id = rawStore.store(payload).id;
            // 2) 저장된 원본을 기준으로 AI 파이프라인을 실행한다.
            pipelineService.process(id, payload);
            // 3) 생성된 식별자를 응답에 담아 호출자가 후속 조회를 할 수 있게 한다.
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", id);
            return ResponseEntity.status(HttpStatus.CREATED).body(ok(data));
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(fail("E-0000", "raw store failed"));
        }
    }

    // 공통 성공 응답 포맷.
    // 이유: 호출자가 항상 같은 구조를 기대할 수 있어 연동이 단순해진다.
    private Map<String, Object> ok(Object data) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("result", "OK");
        res.put("message", "");
        res.put("data", data);
        return res;
    }

    // 공통 실패 응답 포맷.
    // 이유: 실패 원인을 errorCode/message로 전달해 클라이언트가 원인을 판단할 수 있다.
    private Map<String, Object> fail(String errorCode, String message) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("result", "FAIL");
        res.put("message", message);
        res.put("errorCode", errorCode);
        res.put("data", null);
        return res;
    }
}
