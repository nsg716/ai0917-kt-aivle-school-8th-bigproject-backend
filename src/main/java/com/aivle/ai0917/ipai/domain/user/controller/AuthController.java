package com.aivle.ai0917.ipai.domain.user.controller;


import com.aivle.ai0917.ipai.global.security.jwt.JwtProvider;
import com.aivle.ai0917.ipai.infra.naver.dto.NaverLoginResultDto;
import com.aivle.ai0917.ipai.infra.naver.service.NaverAuthService;
import com.aivle.ai0917.ipai.domain.user.model.User;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 네이버 로그인/회원가입 API
 *
 * 동작 방식(추천):
 * 1) 프론트가 /auth/naver/login 호출
 * 2) 백엔드가 네이버 로그인 페이지로 redirect
 * 3) 로그인 완료 후 네이버가 /auth/naver/callback으로 code/state 전달
 * 4) 백엔드는 사용자 조회/가입 후 JWT 발급
 * 5) 프론트 callback url로 토큰을 들고 redirect
 */
@RestController
@RequestMapping("/api/v1/auth/naver")
public class AuthController {

    private final NaverAuthService naverAuthService;
    private final JwtProvider jwtProvider;

    @Value("${app-front.callback-url}")
    private String frontCallbackUrl;

    public AuthController(NaverAuthService naverAuthService, JwtProvider jwtProvider) {
        this.naverAuthService = naverAuthService;
        this.jwtProvider = jwtProvider;
    }

    /** 네이버 로그인 시작: 네이버 authorize URL로 redirect */
    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException {
        var result = naverAuthService.buildLoginUrl();
        response.sendRedirect(result.url());
    }

    /**
     * 네이버 로그인 콜백
     * - code/state로 access_token 발급
     * - access_token으로 프로필 조회
     * - DB에 유저 저장/갱신
     * - JWT 발급 후 프론트로 redirect
     */
    @GetMapping("/callback")
    public void callback(@RequestParam("code") String code,
                         @RequestParam("state") String state,
                         HttpServletResponse response) throws IOException {

        User user = naverAuthService.loginOrRegister(code, state);

        // 우리 서비스 JWT 발급
        String jwt = jwtProvider.createAccessToken(user.getId(), String.valueOf(user.getRole()));

        // 프론트로 redirect + token 전달(간단 버전)
        // 운영에서는 보통 HttpOnly 쿠키 방식 추천
        String encoded = URLEncoder.encode(jwt, StandardCharsets.UTF_8);
        response.sendRedirect(frontCallbackUrl + "?token=" + encoded);
    }

    /**
     * 만약 프론트가 redirect 방식 말고 "JSON으로 토큰 받고 싶다"면
     * 프론트가 code/state를 받아 이 API로 POST하는 구조로 사용 가능
     */
    @PostMapping("/callback")
    public Map<String, Object> callbackJson(@RequestBody Map<String, String> body) {

        String code = body.get("code");
        String state = body.get("state");

        User user = naverAuthService.loginOrRegister(code, state);
        String jwt = jwtProvider.createAccessToken(user.getId(), String.valueOf(user.getRole()));

        return Map.of(
                "accessToken", jwt,
                "userId", user.getId(),
                "name", user.getName(),
                "email", user.getEmail()
        );
    }
    /**
     * 네이버 로그인 처리 API (JSON 응답)
     * - 신규 회원: 이름, 성별, 생일, 출생연도, 휴대전화번호 반환
     * - 기존 회원: 권한(role) 반환
     */
    @PostMapping("/user")
    public Map<String, Object> callbackUserStatus(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        String state = body.get("state");

        // 서비스 호출 (신규 여부 포함된 결과)
        NaverLoginResultDto result = naverAuthService.loginOrRegisterWithStatus(code, state);
        User user = result.user();

        // JWT 토큰 생성
        String jwt = jwtProvider.createAccessToken(user.getId(), String.valueOf(user.getRole()));

        // 응답 데이터 구성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("accessToken", jwt);
        responseData.put("isNewMember", result.isNewMember());

        if (result.isNewMember()) {
            // 신규 회원 전용 정보 추가
            responseData.put("name", user.getName() != null ? user.getName() : "");
            responseData.put("gender", user.getGender() != null ? user.getGender() : "");
            responseData.put("birthday", user.getBirthday() != null ? user.getBirthday() : "");
            responseData.put("birthYear", user.getBirthYear() != null ? user.getBirthYear() : "");
            responseData.put("mobile", user.getMobile() != null ? user.getMobile() : "");
        } else {
            // 기존 회원 전용 정보 추가
            responseData.put("role", user.getRole());
        }

        return responseData;
    }

}