package com.aivle.ai0917.ipai.domain.user.controller;

import com.aivle.ai0917.ipai.infra.naver.dto.ChangePasswordRequest;
import com.aivle.ai0917.ipai.domain.user.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/author")
public class UserPasswordController {

    private final UserService userService;

    public UserPasswordController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 비밀번호 변경
     * PATCH /api/v1/author/{userId}/mypage/pwd
     */
    @PatchMapping("/{userId}/mypage/pwd")
    public Map<String, Object> changeMyPassword(@PathVariable Long userId,
                                                Authentication authentication,
                                                @RequestBody ChangePasswordRequest req) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        Long loginUserId = (Long) authentication.getPrincipal();

        // URL의 userId와 로그인한 userId가 같아야 함
        if (!userId.equals(loginUserId)) {
            throw new RuntimeException("본인 계정만 비밀번호 변경이 가능합니다.");
        }

        userService.changeMySitePassword(
                loginUserId,
                req.currentPassword(),
                req.newPassword(),
                req.newPasswordConfirm()
        );

        return Map.of("ok", true, "message", "비밀번호가 변경되었습니다.");
    }
}
