package com.aivle.ai0917.ipai.domain.user.controller;

import com.aivle.ai0917.ipai.domain.user.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthPasswordController {

    private final UserService userService;

    public AuthPasswordController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 로그인 후(마이페이지) 비밀번호 변경
     * POST /api/v1/auth/password/reset
     * body: { currentPassword, newPassword, newPasswordRetype }
     */
    @PostMapping("/password/reset")
    public Map<String, Object> resetPasswordAfterLogin(@RequestBody Map<String, String> body,
                                                       Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("인증이 필요합니다.");
        }

        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");
        String newPasswordRetype = body.get("newPasswordRetype");

        if (currentPassword == null || currentPassword.isBlank()) throw new RuntimeException("currentPassword가 비어있습니다.");
        if (newPassword == null || newPassword.isBlank()) throw new RuntimeException("newPassword가 비어있습니다.");
        if (newPasswordRetype == null || newPasswordRetype.isBlank()) throw new RuntimeException("newPasswordRetype가 비어있습니다.");
        if (!newPassword.equals(newPasswordRetype)) throw new RuntimeException("비밀번호가 일치하지 않습니다.");


        Long userId = (Long) authentication.getPrincipal();

        userService.changePasswordAfterLogin(userId, currentPassword, newPassword);

        return Map.of("ok", true, "message", "비밀번호가 변경되었습니다.");
    }
}
