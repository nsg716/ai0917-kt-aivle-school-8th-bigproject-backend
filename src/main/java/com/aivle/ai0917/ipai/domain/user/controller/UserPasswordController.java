package com.aivle.ai0917.ipai.domain.user.controller;

import com.aivle.ai0917.ipai.infra.naver.dto.ChangePasswordRequest;
import com.aivle.ai0917.ipai.domain.user.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserPasswordController {

    private final UserService userService;

    public UserPasswordController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("/me/password")
    public Map<String, Object> changeMyPassword(Authentication authentication,
                                                @RequestBody ChangePasswordRequest req) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        Long userId = (Long) authentication.getPrincipal(); // ✅ 너희 프로젝트 방식

        userService.changeMySitePassword(
                userId,
                req.currentPassword(),
                req.newPassword(),
                req.newPasswordConfirm()
        );

        return Map.of("ok", true, "message", "비밀번호가 변경되었습니다.");
    }
}