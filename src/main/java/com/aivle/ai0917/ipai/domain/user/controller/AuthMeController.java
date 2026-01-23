package com.aivle.ai0917.ipai.domain.user.controller;

import com.aivle.ai0917.ipai.domain.user.model.User;
import com.aivle.ai0917.ipai.domain.user.repository.UserRepository;
import com.aivle.ai0917.ipai.global.security.jwt.PendingSignupTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthMeController {

    private static final String PENDING_COOKIE = "pendingSignup";

    private final PendingSignupTokenProvider pendingTokenProvider;
    private final UserRepository userRepository;

    public AuthMeController(PendingSignupTokenProvider pendingTokenProvider,
                            UserRepository userRepository) {
        this.pendingTokenProvider = pendingTokenProvider;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public Map<String, Object> me(HttpServletRequest request, Authentication authentication) {

        // 1) 가입 진행 중이면 pendingSignup 우선
        String pendingToken = readCookie(request, PENDING_COOKIE);
        if (pendingToken != null) {
            var p = pendingTokenProvider.parsePendingToken(pendingToken);
            return Map.of(
                    "type", "PENDING",
                    "naverId", p.naverId(),
                    "name", p.name(),
                    "gender", p.gender(),
                    "birthday", p.birthday(),
                    "birthYear", p.birthYear(),
                    "mobile", p.mobile()
            );
        }

        // 2) 로그인 상태면 사용자 정보 반환 (선택)
        if (authentication != null && authentication.isAuthenticated()) {
            Long userId = (Long) authentication.getPrincipal();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            return Map.of(
                    "type", "AUTH",
                    "userId", user.getIntegrationId(),
                    "role", user.getRole(),
                    "siteEmail", user.getSiteEmail() == null ? "" : user.getSiteEmail(),
                    "name", user.getName() == null ? "" : user.getName()
            );
        }

        // 3) 둘 다 아니면 미인증
        return Map.of("type", "ANON");
    }

    private String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}
