
/*package com.aivle.ai0917.ipai.domain.user.controller;


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

*//**
 * 네이버 로그인/회원가입 API
 *
 * 동작 방식(추천):
 * 1) 프론트가 /auth/naver/login 호출
 * 2) 백엔드가 네이버 로그인 페이지로 redirect
 * 3) 로그인 완료 후 네이버가 /auth/naver/callback으로 code/state 전달
 * 4) 백엔드는 사용자 조회/가입 후 JWT 발급
 * 5) 프론트 callback url로 토큰을 들고 redirect
 *//*
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

    *//** 네이버 로그인 시작: 네이버 authorize URL로 redirect *//*
    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException {
        var result = naverAuthService.buildLoginUrl();
        response.sendRedirect(result.url());
    }

    *//**
     * 네이버 로그인 콜백
     * - code/state로 access_token 발급
     * - access_token으로 프로필 조회
     * - DB에 유저 저장/갱신
     * - JWT 발급 후 프론트로 redirect
     *//*
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

    *//**
     * 만약 프론트가 redirect 방식 말고 "JSON으로 토큰 받고 싶다"면
     * 프론트가 code/state를 받아 이 API로 POST하는 구조로 사용 가능
     *//*
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
    *//**
     * 네이버 로그인 처리 API (JSON 응답)
     * - 신규 회원: 이름, 성별, 생일, 출생연도, 휴대전화번호 반환
     * - 기존 회원: 권한(role) 반환
     *//*
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

}*/


package com.aivle.ai0917.ipai.domain.user.controller;

import com.aivle.ai0917.ipai.domain.user.model.User;
import com.aivle.ai0917.ipai.domain.user.repository.UserRepository;
import com.aivle.ai0917.ipai.global.security.jwt.JwtProvider;
import com.aivle.ai0917.ipai.global.security.jwt.PendingSignupTokenProvider;
import com.aivle.ai0917.ipai.infra.naver.service.NaverAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth/naver")
public class AuthController {

    // ✅ 쿠키 이름들
    private static final String OAUTH_STATE_COOKIE = "oauth_state";     // 네이버 OAuth CSRF 방지용 state
    private static final String PENDING_COOKIE     = "pendingSignup";   // 네이버 프로필 임시 보관(가입 진행용)
    private static final String ACCESS_COOKIE      = "accessToken";     // 로그인 완료 JWT

    private final NaverAuthService naverAuthService;
    private final PendingSignupTokenProvider pendingTokenProvider;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    // ✅ 프론트 베이스 URL (로컬: http://localhost:5173)
    @Value("${app-front.callback-url}")
    private String frontBaseUrl;

    // ✅ 로컬에서는 HTTP이므로 secure=false가 기본
    // ✅ AWS(HTTPS) 배포 시에는 secure=true 로 바꾸는 게 원칙
    @Value("${security.cookie.secure:false}")
    private boolean cookieSecure;

    // ✅ 로컬은 Lax 권장
    // ✅ 프론트/백엔드가 서로 다른 "도메인"이면(AWS 배포) SameSite=None + Secure=true 필요
    @Value("${security.cookie.same-site:Lax}")
    private String sameSite;

    // ✅ pendingSignup(임시 가입 토큰) 만료 분 (너무 길게 잡지 말 것)
    @Value("${security.pending.exp-minutes:10}")
    private long pendingExpMinutes;

    public AuthController(
            NaverAuthService naverAuthService,
            PendingSignupTokenProvider pendingTokenProvider,
            UserRepository userRepository,
            JwtProvider jwtProvider
    ) {
        this.naverAuthService = naverAuthService;
        this.pendingTokenProvider = pendingTokenProvider;
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
    }

    /**
     * 1) 네이버 로그인 시작
     * - state 생성
     * - oauth_state를 HttpOnly 쿠키로 저장
     * - 네이버 로그인 페이지로 redirect
     */
    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException {

        var result = naverAuthService.buildLoginUrl();

        // ✅ state를 쿠키에 저장(서버 저장 없이도 검증 가능)
        // 로컬: Secure=false, SameSite=Lax
        // AWS(HTTPS + 도메인 분리): Secure=true, SameSite=None도 가능(단 None이면 Secure=true 필수)
        ResponseCookie stateCookie = ResponseCookie.from(OAUTH_STATE_COOKIE, result.state())
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/api/v1/auth/naver") // ✅ state는 네이버 콜백에서만 쓰므로 범위 좁힘
                .sameSite("Lax")
                .maxAge(Duration.ofMinutes(5))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, stateCookie.toString());

        response.sendRedirect(result.url());
    }

    /**
     * 2) 네이버 콜백
     * - code/state로 네이버 프로필 조회
     * - ❌ 여기서 DB 저장하면 안 됨(너의 요구사항)
     * - ✅ pendingSignup 쿠키에 "임시 가입 토큰"을 저장
     * - 프론트의 추가 입력 페이지(/signup/naver)로 redirect
     */
    @GetMapping("/callback")
    public void callback(@RequestParam("code") String code,
                         @RequestParam("state") String state,
                         HttpServletRequest request,
                         HttpServletResponse response) throws IOException {

        // (1) CSRF 방지: state 검증
        String stateInCookie = readCookie(request, OAUTH_STATE_COOKIE);
        if (stateInCookie == null || !stateInCookie.equals(state)) {
            // 프론트 쪽에서 오류 화면을 띄우도록 보내도 됨
            response.sendRedirect(frontBaseUrl + "/login?error=invalid_state");
            return;
        }

        // (2) state는 1회용이므로 삭제
        ResponseCookie deleteState = ResponseCookie.from(OAUTH_STATE_COOKIE, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/api/v1/auth/naver")
                .sameSite("Lax")
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteState.toString());

        // (3) 네이버 프로필 조회 (DB 저장 X)
        var profile = naverAuthService.fetchProfile(code, state);
        String naverId = profile.getId();

        // (4) 이미 가입 완료(= siteEmail이 있는 유저)라면 바로 로그인 처리
        Optional<User> existing = userRepository.findByNaverId(naverId);
        if (existing.isPresent() && existing.get().getSiteEmail() != null) {
            User user = existing.get();

            String accessJwt = jwtProvider.createAccessToken(user.getId(), user.getRole());
            ResponseCookie accessCookie = ResponseCookie.from(ACCESS_COOKIE, accessJwt)
                    .httpOnly(true)
                    .secure(cookieSecure)
                    .path("/")
                    .sameSite(sameSite)
                    .maxAge(Duration.ofMinutes(60))
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

            // 로그인 완료 후 프론트가 처리할 callback 페이지로 이동
            response.sendRedirect(frontBaseUrl + "/auth/callback");
            return;
        }

        // (5) 신규/미완료 유저 => pendingSignup 쿠키에 네이버 값 담아두기
        // ✅ 네이버 email은 "없을 수도 있다"고 했으니 여기서는 아예 저장하지 않음(빈값 처리)
        var pendingProfile = new PendingSignupTokenProvider.PendingProfile(
                naverId,
                nullToEmpty(profile.getName()),
                nullToEmpty(profile.getGender()),
                nullToEmpty(profile.getBirthday()),
                nullToEmpty(profile.getBirthyear()),
                nullToEmpty(profile.getMobile())
        );

        String pendingJwt = pendingTokenProvider.createPendingToken(pendingProfile, pendingExpMinutes);

        // ✅ 로컬은 Lax로 OK(같은 localhost라 쿠키 잘 감)
        // ✅ AWS에서 프론트/백엔드가 다른 "도메인"이면 SameSite=None + Secure=true로 바꿔야 함
        ResponseCookie pendingCookie = ResponseCookie.from(PENDING_COOKIE, pendingJwt)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofMinutes(pendingExpMinutes))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, pendingCookie.toString());

        // (6) 프론트의 "추가 입력 페이지"로 이동
        response.sendRedirect(frontBaseUrl + "/signup");
    }

    private String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    private String nullToEmpty(String v) {
        return v == null ? "" : v;
    }
}
