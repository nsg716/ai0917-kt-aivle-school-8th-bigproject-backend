package com.aivle.ai0917.ipai.domain.admin.dashboard.scheduler;

import com.aivle.ai0917.ipai.domain.admin.dashboard.model.DailyActiveUser;
import com.aivle.ai0917.ipai.domain.admin.dashboard.model.SystemLog;
import com.aivle.ai0917.ipai.domain.admin.dashboard.model.SystemMetric;
import com.aivle.ai0917.ipai.domain.admin.dashboard.repository.DailyActiveUserRepository;
import com.aivle.ai0917.ipai.domain.admin.dashboard.repository.SystemLogRepository;
import com.aivle.ai0917.ipai.domain.admin.dashboard.repository.SystemMetricRepository;

import com.aivle.ai0917.ipai.domain.admin.info.dto.UnifiedAdminNoticeDto;
import com.aivle.ai0917.ipai.domain.admin.info.service.AdminNoticeService;
import com.aivle.ai0917.ipai.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.management.OperatingSystemMXBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.time.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 대시보드 데이터 자동 수집 스케줄러
 * - 리소스 사용량: 5분마다
 * - DAU: 매일 자정
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataCollectionScheduler {

    private final SystemMetricRepository systemMetricRepository;
    private final DailyActiveUserRepository dauRepository;
    private final UserRepository userRepository;
    private final SystemLogRepository systemLogRepository;
    private final AdminNoticeService adminNoticeService;
    private final ObjectMapper objectMapper;

    // 샘플링 간격 (5초)
    private static final long SAMPLING_INTERVAL_MS = 5000;

    /**
     * 시스템 리소스 메트릭 수집 (5분마다)
     * - 5초 간격 샘플링으로 평균 측정
     * - 메트릭 수집 및 저장
     * - 임계치 초과 시 SystemLog에 저장 (기존 로직 유지)
     * - 임계치 초과 시 실시간 SSE 알림 전송 (신규)
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void collectSystemMetrics() {
        try {
            log.info("Collecting system metrics with 5-second sampling...");

            // 5초 샘플링으로 평균 측정
            OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            Runtime runtime = Runtime.getRuntime();

            // 첫 번째 측정
            MetricSample startSample = measureMetrics(osBean, runtime);

            // 5초 대기
            Thread.sleep(SAMPLING_INTERVAL_MS);

            // 두 번째 측정
            MetricSample endSample = measureMetrics(osBean, runtime);

            // 평균 계산
            double cpuUsage = (startSample.cpuUsage + endSample.cpuUsage) / 2.0;
            double memoryUsage = (startSample.memoryUsage + endSample.memoryUsage) / 2.0;
            double storageUsage = (startSample.storageUsage + endSample.storageUsage) / 2.0;

            // 임계치 체크 (예: 90% 이상일 때 에러 로그 생성)
            if (cpuUsage > 90.0 || memoryUsage > 90.0 || storageUsage > 90.0) {
                // 1. SystemLog에 저장 (기존 로직 유지)
                String logMessage = String.format("리소스 임계치 초과! CPU: %.2f%%, RAM: %.2f%%, Storage: %.2f%%",
                        cpuUsage, memoryUsage, storageUsage);

                SystemLog criticalLog = SystemLog.builder()
                        .level("WARNING")
                        .category("RESOURCE_CRITICAL")
                        .message(logMessage)
                        .timestamp(LocalDateTime.now())
                        .isRead(false)
                        .build();

                SystemLog savedLog = systemLogRepository.save(criticalLog);
                log.warn("Critical resource threshold exceeded - SystemLog saved: id={}", savedLog.getId());

                // 2. 메타데이터 생성
                String metadata = buildMetadata(cpuUsage, memoryUsage, storageUsage);

                // 3. 실시간 알림 전송 (신규 - admin_notices에도 저장됨)
                try {
                    adminNoticeService.sendSystemMetricAlert(
                            "RESOURCE_CRITICAL",
                            logMessage,
                            metadata
                    );
                    log.info("Real-time metric alert sent via SSE");
                } catch (Exception e) {
                    log.error("Failed to send real-time metric alert", e);
                }
            }

            // 메트릭 저장 (기존 로직 유지)
            SystemMetric metric = SystemMetric.builder()
                    .cpuUsage(Math.round(cpuUsage * 100.0) / 100.0)
                    .memoryUsage(Math.round(memoryUsage * 100.0) / 100.0)
                    .storageUsage(Math.round(storageUsage * 100.0) / 100.0)
                    .timestamp(LocalDateTime.now())
                    .build();

            systemMetricRepository.save(metric);
            log.info("System metrics saved: CPU={}%, Memory={}%, Storage={}%",
                    metric.getCpuUsage(), metric.getMemoryUsage(), metric.getStorageUsage());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Sampling interrupted", e);
        } catch (Exception e) {
            log.error("Failed to collect system metrics", e);
        }
    }

    /**
     * 현재 시스템 메트릭 측정
     */
    private MetricSample measureMetrics(OperatingSystemMXBean osBean, Runtime runtime) {
        // CPU 사용률
        double cpuUsage = osBean.getSystemCpuLoad() * 100.0;
        if (cpuUsage < 0) {
            cpuUsage = osBean.getProcessCpuLoad() * 100.0;
        }
        if (cpuUsage < 0) cpuUsage = 0.0;

        // 메모리 사용률
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        double memoryUsage = ((double)(totalMemory - freeMemory) / totalMemory) * 100.0;

        // 스토리지 사용률
        File root = new File("/");
        long totalSpace = root.getTotalSpace();
        long freeSpace = root.getFreeSpace();
        double storageUsage = totalSpace > 0
                ? ((double)(totalSpace - freeSpace) / totalSpace) * 100.0
                : 0.0;

        return new MetricSample(cpuUsage, memoryUsage, storageUsage);
    }
    /**
     * 메타데이터 JSON 생성
     */
    private String buildMetadata(double cpu, double memory, double storage) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("cpuUsage", Math.round(cpu * 100.0) / 100.0);
            metadata.put("cpuThreshold", 90.0);
            metadata.put("memoryUsage", Math.round(memory * 100.0) / 100.0);
            metadata.put("memoryThreshold", 90.0);
            metadata.put("storageUsage", Math.round(storage * 100.0) / 100.0);
            metadata.put("storageThreshold", 90.0);
            metadata.put("timestamp", LocalDateTime.now().toString());

            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            log.error("Failed to build metadata JSON", e);
            return "{}";
        }
    }

    /**
     * 일일 활성 사용자(DAU) 계산
     * 매일 자정 00:05에 실행 (전날 데이터 집계)
     */
    @Scheduled(cron = "0 5 0 * * *")
    public void calculateDailyActiveUsers() {
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDateTime start = yesterday.atStartOfDay();
            LocalDateTime end = yesterday.atTime(23, 59, 59);

            // 1. UserRepository를 통해 users 테이블에서 전날 활동 유저 수 집계
            Integer activeUserCount = userRepository.countActiveUsersBetween(start, end);

            // 2. 통계 테이블(DailyActiveUser)에 저장
            DailyActiveUser dau = DailyActiveUser.builder()
                    .date(start)
                    .count(activeUserCount)
                    .createdAt(LocalDateTime.now())
                    .build();

            dauRepository.save(dau);
            log.info("Successfully aggregated DAU for {}: {}", yesterday, activeUserCount);
        } catch (Exception e) {
            log.error("Failed to aggregate DAU", e);
        }
    }

    /**
     * 애플리케이션 시작 시 초기 메트릭 수집
     */
    @Scheduled(initialDelay = 10000, fixedDelay = Long.MAX_VALUE)
    public void collectInitialMetrics() {
        log.info("Collecting initial system metrics on startup...");
        collectSystemMetrics();
    }

    /**
     * 메트릭 샘플 내부 클래스
     */
    private static class MetricSample {
        double cpuUsage;
        double memoryUsage;
        double storageUsage;

        MetricSample(double cpu, double memory, double storage) {
            this.cpuUsage = cpu;
            this.memoryUsage = memory;
            this.storageUsage = storage;
        }
    }
}