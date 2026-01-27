package com.aivle.ai0917.ipai.global.security.interceptor;

import com.aivle.ai0917.ipai.domain.admin.access.model.UserRole;
import com.aivle.ai0917.ipai.domain.user.model.User;
import com.aivle.ai0917.ipai.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class LastActivityInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 1. Principal 확인 로그 추가 (디버깅용)
        if (auth == null || !auth.isAuthenticated()) {
            return true; // 비인증 사용자는 그냥 통과
        }

        Object principal = auth.getPrincipal();

        // 2. Principal이 Long(UserId)인 경우에만 로직 수행
        if (principal instanceof Long userId) {
            userRepository.findById(userId).ifPresent(user -> {

                // 최근 1분 이내면 업데이트 스킵
                if (user.getRole() == UserRole.Deactivated) {
                    return;
                }
                if (user.getLastActivityAt() == null ||
                        user.getLastActivityAt().isBefore(LocalDateTime.now().minusMinutes(1))) {

                    userRepository.updateLastActivity(userId, LocalDateTime.now());

                    log.info("사용자(ID: {}) 활동 시각 갱신 완료", userId);
                }
            });
        } else {
            // principal이 Long이 아닌 경우(예: "anonymousUser" 문자열) 로그
            log.debug("인증은 되었으나 Principal이 Long 타입이 아님: {}", principal);
        }

        return true;
    }
}