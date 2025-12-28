package com.mes.gateway;

public enum GatewayLogReason {
    // 성공 또는 사유가 필요 없는 경우를 표현한다.
    // 이유: PASS 라인은 간결해야 하므로 필요 시에만 사유를 출력한다.
    NONE,
    // 입력 인자가 잘못된 경우.
    INVALID_ARGS,
    // 업링크 목적지 URL이 없는 경우.
    UPLINK_URL_MISSING,
    // 업링크 전송 중 네트워크 예외가 발생한 경우.
    UPLINK_SEND_ERROR,
    // 업링크 응답 코드가 기대와 다른 경우.
    UPLINK_BAD_STATUS,
    // 다운링크 URL이 없는 경우.
    DOWNLINK_URL_MISSING,
    // 다운링크 조회 결과 명령이 없는 경우.
    DOWNLINK_NO_COMMAND,
    // 다운링크 응답 코드가 기대와 다른 경우.
    DOWNLINK_BAD_STATUS,
    // 다운링크 전송 중 네트워크 예외가 발생한 경우.
    DOWNLINK_SEND_ERROR,
    // 다운링크 응답 저장에 실패한 경우.
    DOWNLINK_OUTPUT_WRITE_ERROR
}
