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
     * 매일 새벽 3시에 실행
     */
    @Transactional // 외부(스케줄러)에서 호출하므로 작동함
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupDeactivatedUsers() {
        performCleanup();
    }

    /**
     * 서버 시작 시 초기 실행
     */
    @Transactional // ✅ 여기에 추가해야 합니다! (내부 호출 시 트랜잭션 보장을 위해)
    @Scheduled(initialDelay = 30000, fixedDelay = Long.MAX_VALUE)
    public void onStartupCleanup() {
        log.info("서버 시작 시 초기 사용자 데이터 정리를 수행합니다.");
        performCleanup();
    }

    // 실제 로직을 별도 메서드로 분리 (선택 사항)
    private void performCleanup() {
        log.info("정기 데이터 정리 시작: 7일 이상 경과한 탈퇴 계정을 삭제합니다.");
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);

        try {
            int deletedCount = userRepository.deleteExpiredDeactivatedUsers(UserRole.Deactivated, threshold);
            log.info("정리 완료: 총 {}명의 사용자 데이터가 영구 삭제되었습니다.", deletedCount);
        } catch (Exception e) {
            log.error("사용자 데이터 정리 중 오류 발생", e);
            throw e; // 트랜잭션 롤백을 위해 던짐
        }
    }
}