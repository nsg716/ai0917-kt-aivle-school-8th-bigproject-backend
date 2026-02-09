package com.aivle.ai0917.ipai.domain.author.info.scheduler;

import com.aivle.ai0917.ipai.domain.author.episodes.repository.ManuscriptCommandRepository;
import com.aivle.ai0917.ipai.domain.author.episodes.repository.ManuscriptRepository;
import com.aivle.ai0917.ipai.domain.author.lorebook.repository.SettingBookCommandRepository;
import com.aivle.ai0917.ipai.domain.author.lorebook.repository.SettingBookViewRepository;
import com.aivle.ai0917.ipai.domain.author.works.repository.WorkCommandRepository;
import com.aivle.ai0917.ipai.domain.author.works.repository.WorkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 작가 데이터 자동 정리 스케줄러
 * - 대상: Works, Episodes, Lorebooks
 * - 조건: deleted_at(삭제 요청 시간)으로부터 1주일(7일) 이상 경과한 데이터
 * - 실행 시점: 매일 새벽 3시 & 서버 시작 시
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorDataCleanupScheduler {

    private final WorkCommandRepository workCommandRepository;
    private final ManuscriptCommandRepository manuscriptCommandRepository;
    private final SettingBookCommandRepository commandRepository;

    /**
     * 매일 새벽 03:00 실행
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void scheduleDailyCleanup() {
        log.info("Starting scheduled daily cleanup for soft-deleted author data...");
        performCleanup();
        log.info("Daily cleanup completed.");
    }

    /**
     * 서버 시작 시 1회 실행 (시작 후 10초 뒤)
     */
    @Scheduled(initialDelay = 10000, fixedDelay = Long.MAX_VALUE)
    @Transactional
    public void cleanupOnStartup() {
        log.info("Starting on-startup cleanup for soft-deleted author data...");
        performCleanup();
        log.info("Startup cleanup completed.");
    }

    /**
     * 실제 삭제 로직 수행
     */
    private void performCleanup() {
        // 기준 시간: 현재 시간으로부터 7일 전
        LocalDateTime threshold = LocalDateTime.now().minusWeeks(1);
        log.info("Cleanup Threshold Date: {}", threshold);

        try {
            // 1. Lorebooks 정리
            // (참고: 외래 키 제약 조건에 따라 자식 테이블을 먼저 지우거나 Cascade 설정이 필요할 수 있음)
            long deletedLorebooks = commandRepository.deleteByDeletedAtBefore(threshold);
            log.info("Deleted {} old lorebooks.", deletedLorebooks);

            // 2. Episodes 정리
            long deletedEpisodes = manuscriptCommandRepository.deleteByDeletedAtBefore(threshold);
            log.info("Deleted {} old episodes.", deletedEpisodes);

            // 3. Works 정리
            long deletedWorks = workCommandRepository.deleteByDeletedAtBefore(threshold);
            log.info("Deleted {} old works.", deletedWorks);

        } catch (Exception e) {
            log.error("Failed to cleanup author data", e);
        }
    }
}