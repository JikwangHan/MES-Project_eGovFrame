package com.mes.web.ui;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UiController {
    // -------------------------------------------------------------
    // UI 진입 컨트롤러
    // 목적: 가장 기본적인 UI 진입점을 제공한다.
    // 이유: 화면이 정상 동작하는지 빠르게 확인해야 하기 때문이다.
    // -------------------------------------------------------------
    // 초보자 설명:
    // - "/ui"는 사용자가 처음 들어오는 기본 화면이다.
    // - 복잡한 기능이 없어도 "접속이 된다"는 것 자체가 중요하다.
    // 목적: UI 스캐폴딩의 최소 진입점을 제공한다.
    // 이유: 화면 라우팅과 레이아웃 골격이 준비되었는지 스모크로 빠르게 확인하기 위함이다.
    // 입력: /ui 경로 요청.
    // 출력: 공통 레이아웃 HTML.
    
    // 목적: HTML 응답에 UTF-8을 명시해 한글 깨짐을 방지한다.
    // 이유: 브라우저 인코딩 자동 판단이 실패할 수 있기 때문이다.
    @GetMapping(value = "/ui", produces = "text/html; charset=UTF-8")

    public String uiHome() {
        return UiPageTemplate.render(
            "MES UI",
            "MES UI 스캐폴딩",
            "화면 골격 준비 완료. 데이터 바인딩은 후속 단계에서 적용합니다.",
            "/ui"
        );
    }
}
