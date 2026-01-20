package com.aivle.ai0917.ipai.domain.admin.dashboard.scheduler;

import com.aivle.ai0917.ipai.domain.admin.dashboard.model.DailyActiveUser;
import com.aivle.ai0917.ipai.domain.admin.dashboard.model.SystemMetric;
import com.aivle.ai0917.ipai.domain.admin.dashboard.repository.DailyActiveUserRepository;
import com.aivle.ai0917.ipai.domain.admin.dashboard.repository.SystemMetricRepository;

import com.aivle.ai0917.ipai.domain.user.repository.UserRepository;
import com.sun.management.OperatingSystemMXBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.time.*;

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

    // 시스템 리소스 메트릭 수집
    // 5분마다 실행 (cron: 0 */5 * * * *)

    @Scheduled(cron = "0 */5 * * * *")
    public void collectSystemMetrics() {
        try {
            log.info("Collecting system metrics...");

            OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            Runtime runtime = Runtime.getRuntime();

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

            SystemMetric metric = SystemMetric.builder()
                    .cpuUsage(Math.round(cpuUsage * 100.0) / 100.0)
                    .memoryUsage(Math.round(memoryUsage * 100.0) / 100.0)
                    .storageUsage(Math.round(storageUsage * 100.0) / 100.0)
                    .timestamp(LocalDateTime.now())
                    .build();

            systemMetricRepository.save(metric);
            log.info("System metrics saved: CPU={}%, Memory={}%, Storage={}%",
                    metric.getCpuUsage(), metric.getMemoryUsage(), metric.getStorageUsage());

        } catch (Exception e) {
            log.error("Failed to collect system metrics", e);
        }
    }

    /**
     * 일일 활성 사용자(DAU) 계산
     * 매일 자정 00:05에 실행 (전날 데이터 집계)
     */
    @Scheduled(cron = "0 5 0 * * *")
    public void calculateDailyActiveUsers() {
        try {
            log.info("Calculating Daily Active Users...");

            // Instant로 어제 날짜의 시작과 끝 계산
            ZoneId zoneId = ZoneId.systemDefault();
            Instant now = Instant.now();

            // 어제 날짜
            ZonedDateTime yesterdayZoned = now.atZone(zoneId).minusDays(1);

            // 어제의 00:00:00
            Instant startOfDay = yesterdayZoned.toLocalDate()
                    .atStartOfDay(zoneId)
                    .toInstant();

            // 어제의 23:59:59
            Instant endOfDay = yesterdayZoned.toLocalDate()
                    .atTime(23, 59, 59)
                    .atZone(zoneId)
                    .toInstant();

            // LocalDateTime으로 변환하여 Repository 쿼리 호출
            LocalDateTime startDateTime = LocalDateTime.ofInstant(startOfDay, zoneId);
            LocalDateTime endDateTime = LocalDateTime.ofInstant(endOfDay, zoneId);

            // 전날 활성 사용자 수 계산
            Integer activeUserCount = userRepository.countActiveUsersBetween( startDateTime, endDateTime);

            DailyActiveUser dau = DailyActiveUser.builder()
                    .date(startDateTime)
                    .count(activeUserCount)
                    .createdAt(LocalDateTime.now())
                    .build();

            dauRepository.save(dau);
            log.info("DAU saved: Date={}, Count={}", startDateTime.toLocalDate(), activeUserCount);

        } catch (Exception e) {
            log.error("Failed to calculate DAU", e);
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
}