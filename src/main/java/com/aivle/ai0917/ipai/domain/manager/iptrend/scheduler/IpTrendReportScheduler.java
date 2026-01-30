package com.aivle.ai0917.ipai.domain.manager.iptrend.scheduler;

import com.aivle.ai0917.ipai.domain.manager.iptrend.dto.IpTrendResponseDto.GenerateReportRequest;
import com.aivle.ai0917.ipai.domain.manager.iptrend.dto.IpTrendResponseDto.GenerateReportResponse;
import com.aivle.ai0917.ipai.domain.manager.iptrend.service.IpTrendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * IP 트렌드 분석 리포트 자동 생성 스케줄러
 *
 * 매월 1일 새벽 2시에 자동으로 트렌드 분석 리포트를 생성합니다.
 * application.yml의 iptrend.scheduler.enabled 값으로 활성화/비활성화 가능
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "iptrend.scheduler.enabled",
        havingValue = "true",
        matchIfMissing = false  // 설정이 없으면 비활성화
)
public class IpTrendReportScheduler {

    private final IpTrendService ipTrendService;

    /**
     * 매월 1일 새벽 2시에 IP 트렌드 리포트 자동 생성
     *
     * Cron 표현식: "0 0 2 1 * ?"
     * - 초: 0
     * - 분: 0
     * - 시: 2 (새벽 2시)
     * - 일: 1 (매월 1일)
     * - 월: * (모든 월)
     * - 요일: ? (상관없음)
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    public void generateMonthlyReport() {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        log.info("IP 트렌드 월간 리포트 자동 생성 시작: {}", timestamp);

        try {
            // 이미 오늘 리포트가 존재하는지 확인
            boolean existsToday = ipTrendService.isReportExistsToday();

            if (existsToday) {
                log.warn("⚠️ 오늘 날짜의 리포트가 이미 존재합니다. 생성을 건너뜁니다.");
                log.info("기존 리포트 확인이 필요한 경우 대시보드를 확인하세요.");
                return;
            }

            // 리포트 생성 요청 생성
            GenerateReportRequest request = GenerateReportRequest.builder()
                    .analysisDate(now)
                    .dataSource("Google Trends")
                    .forceRegenerate(false)
                    .build();

            // 리포트 생성 실행
            log.info("→ 리포트 생성 요청 시작...");
            GenerateReportResponse response = ipTrendService.generateNewReport(request);

            log.info("✅ 리포트 생성 요청 성공!");
            log.info("   - Report ID: {}", response.getReportId());
            log.info("   - File Name: {}", response.getFileName());
            log.info("   - Status: {}", response.getStatus());
            log.info("   - Message: {}", response.getMessage());
            log.info("");
            log.info("📌 리포트는 백그라운드에서 생성됩니다. 완료까지 5-10분 소요될 수 있습니다.");

        } catch (IllegalStateException e) {
            log.error("❌ 리포트 생성 실패 (상태 오류): {}", e.getMessage());
            handleSchedulerError(e);

        } catch (Exception e) {
            log.error("❌ 리포트 생성 중 예상치 못한 오류 발생", e);
            handleSchedulerError(e);

        } finally {
            log.info("===============================================");
            log.info("IP 트렌드 월간 리포트 자동 생성 종료: {}",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            log.info("===============================================");
        }
    }

    /**
     * 수동 테스트용 메서드 (개발/디버깅용)
     *
     * 스케줄러를 기다리지 않고 즉시 리포트를 생성하고 싶을 때 사용
     * 이 메서드는 @Scheduled 어노테이션이 없어서 자동 실행되지 않음
     *
     * 사용법: 별도 API 엔드포인트를 만들어서 호출하거나, 직접 메서드 호출
     */
    public void generateReportManually() {
        log.info("📝 수동 리포트 생성 요청");
        generateMonthlyReport();
    }

    /**
     * 스케줄러 오류 처리
     *
     * 필요시 이메일 알림, Slack 알림 등을 추가할 수 있음
     */
    private void handleSchedulerError(Exception e) {
        log.error("스케줄러 오류 처리 시작");

        // TODO: 실패 알림 전송 (선택 사항)
        // - 이메일 알림
        // - Slack 알림
        // - 모니터링 시스템 연동

        log.error("관리자에게 알림을 전송해야 합니다: {}", e.getMessage());
    }


}