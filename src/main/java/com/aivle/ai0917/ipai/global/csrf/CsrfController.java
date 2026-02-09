package com.aivle.ai0917.ipai.global.csrf;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CsrfController {

    @GetMapping("/api/v1/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        // token을 "조회"하는 순간 CookieCsrfTokenRepository가 XSRF-TOKEN 쿠키를 내려줌
        return Map.of("token", token.getToken());
    }
}
