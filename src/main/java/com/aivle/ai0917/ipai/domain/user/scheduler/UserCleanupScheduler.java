package com.aivle.ai0917.ipai.domain.user.scheduler;

import com.aivle.ai0917.ipai.domain.admin.access.model.UserRole;
import com.aivle.ai0917.ipai.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {

    private final UserRepository userRepository;

    /**
     * 매일 새벽 3시에 실행 (7일 경과 탈퇴 계정 영구 삭제)
     */
    @Transactional
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupDeactivatedUsers() {
        log.info("정기 데이터 정리 시작: 7일 이상 경과한 탈퇴 계정을 삭제합니다.");

        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        int deletedCount = userRepository.deleteExpiredDeactivatedUsers(UserRole.Deactivated, threshold);

        if (deletedCount > 0) {
            log.info("정리 완료: 총 {}명의 사용자 데이터가 영구 삭제되었습니다.", deletedCount);
        } else {
            log.info("정리 완료: 삭제할 대상 사용자가 없습니다.");
        }
    }

    /**
     * 서버 시작 시 30초 후 초기 실행
     */
    @Scheduled(initialDelay = 30000, fixedDelay = Long.MAX_VALUE)
    public void onStartupCleanup() {
        log.info("서버 시작 시 초기 사용자 데이터 정리를 수행합니다.");
        cleanupDeactivatedUsers();
    }
}