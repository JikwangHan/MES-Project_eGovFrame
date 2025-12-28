package com.mes.gateway.uplink;

public class UplinkNormalizer {
    // 업링크 데이터 정규화 스캐폴딩.
    // 이유: 현재는 단순 통과 처리지만, 추후 표준 텔레메트리 모델로 변환할 위치를 명확히 한다.
    public String normalize(String rawPayload) {
        return rawPayload == null ? "" : rawPayload;
    }
}
