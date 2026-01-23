package com.aivle.ai0917.ipai.domain.user.controller;



import com.aivle.ai0917.ipai.infra.naver.dto.LoginRequest;
import com.aivle.ai0917.ipai.domain.user.model.User;
import com.aivle.ai0917.ipai.domain.user.repository.UserRepository;
import com.aivle.ai0917.ipai.global.security.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthEmailController {

    private static final String ACCESS_COOKIE = "accessToken";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Value("${security.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${security.cookie.same-site:Lax}")
    private String sameSite;

    // accessToken 쿠키 유효시간(분). 필요하면 yml로 빼도 됨
    @Value("${security.access.exp-minutes:60}")
    private long accessExpMinutes;

    public AuthEmailController(UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    /**
     * ✅ 이메일 + 비밀번호 로그인
     * body: { "siteEmail": "...", "sitePwd": "..." }
     * 성공: accessToken(HttpOnly 쿠키) 발급
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest req,
                                     HttpServletResponse response) {

        String siteEmail = req.siteEmail();
        String sitePwd   = req.sitePwd();

        if (siteEmail == null || siteEmail.isBlank()) {
            throw new RuntimeException("siteEmail이 비어있습니다.");
        }
        if (sitePwd == null || sitePwd.isBlank()) {
            throw new RuntimeException("sitePwd가 비어있습니다.");
        }

        User user = userRepository.findBySiteEmail(siteEmail)
                .orElseThrow(() -> new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // ✅ 네이버 미완료 계정 등, 비번이 없으면 로그인 불가
        if (user.getSitePwd() == null || user.getSitePwd().isBlank()) {
            throw new RuntimeException("이 계정은 이메일/비밀번호 로그인이 불가능합니다. (네이버 가입 완료 필요)");
        }

        // ✅ BCrypt 검증 (가입 때 passwordEncoder.encode()로 저장했기 때문)
        if (!passwordEncoder.matches(sitePwd, user.getSitePwd())) {
            throw new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // ✅ JWT 발급 + HttpOnly 쿠키 세팅
        String accessJwt = jwtProvider.createAccessToken(user.getId(), user.getRole());

        ResponseCookie accessCookie = ResponseCookie.from(ACCESS_COOKIE, accessJwt)
                .httpOnly(true)
                .secure(cookieSecure)      // 로컬 false / HTTPS true
                .path("/")
                .sameSite(sameSite)        // 로컬 Lax / 도메인 분리면 None(+Secure=true)
                .maxAge(Duration.ofMinutes(accessExpMinutes))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

        return Map.of(
                "ok", true,
                "userId",user.getIntegrationId(),
                "role", user.getRole()
        );
    }

    /**
     * (선택) 로그아웃: accessToken 쿠키 삭제
     */
    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletResponse response) {
        ResponseCookie delete = ResponseCookie.from(ACCESS_COOKIE, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .sameSite(sameSite)
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, delete.toString());
        return Map.of("ok", true);
    }
}
