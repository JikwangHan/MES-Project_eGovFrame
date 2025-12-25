package com.mes.web.ui;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UiController {
    // 목적: UI 스캐폴딩의 최소 진입점을 제공한다.
    // 이유: 화면 라우팅과 레이아웃 골격이 준비되었는지 스모크로 빠르게 확인하기 위함이다.
    @GetMapping(value = "/ui", produces = MediaType.TEXT_HTML_VALUE)
    public String uiHome() {
        return UiPageTemplate.render(
            "MES UI",
            "MES UI 스캐폴딩",
            "화면 골격 준비 완료. 데이터 바인딩은 후속 단계에서 적용합니다.",
            "/ui"
        );
    }
}
