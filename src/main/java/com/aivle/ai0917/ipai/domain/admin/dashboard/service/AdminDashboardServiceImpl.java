package com.aivle.ai0917.ipai.domain.admin.dashboard.service;

import com.aivle.ai0917.ipai.domain.admin.dashboard.dto.*;
import com.aivle.ai0917.ipai.domain.admin.dashboard.model.*;
import com.aivle.ai0917.ipai.domain.admin.dashboard.repository.*;
import com.aivle.ai0917.ipai.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.time.Duration;

import java.time.LocalDate;
import java.time.LocalDateTime; // LocalDateTime 임포트 유지
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final SystemLogRepository systemLogRepository;
    private final DailyActiveUserRepository dauRepository;
    private final SystemMetricRepository systemMetricRepository;
    private final DeploymentInfoRepository deploymentInfoRepository;
    private final UserRepository userRepository;
    private final ArtworkRepository artworkRepository;
    private final LorebookRepository lorebookRepository;

    @Override
    public DashboardPageResponseDto getDashboardPage() {
        return DashboardPageResponseDto.builder()
                .summary(getDashboardSummary())
                .dau(getDauData())
                .resources(getResourceUsage())
                .logs(getRecentLogs(20))
                .deployment(getDeploymentInfo())

                .build();
    }

    @Override
    public DashboardSummaryResponseDto getDashboardSummary() {
        ServerStatusDto serverStatus = checkServerStatus();
        Long totalUsers = userRepository.countTotalUsers();

        // 활성 세션 기준: Instant 대신 LocalDateTime 사용 (User 엔티티 필드 타입에 맞춤)
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        Integer activeSessions = userRepository.countActiveSessions(threshold);
        Long saveArtworks = artworkRepository.countSaveArtworks();
        Long savedLorebooks = lorebookRepository.countSavedLorebooks();

        return DashboardSummaryResponseDto.builder()
                .serverStatus(serverStatus)
                .totalUsers(totalUsers)
                .activeSessions(activeSessions)
                .savedArtworks(saveArtworks)
                .savedLorebooks(savedLorebooks)
                .build();
    }

    @Override
    public DauResponseDto getDauData() {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);

        // 💡 오늘 DAU는 아직 스케줄러가 돌기 전이므로 users 테이블에서 실시간 집계
        Integer todayDau = userRepository.countActiveUsersBetween(today.atStartOfDay(), LocalDateTime.now());

        // 어제 DAU (통계 테이블 조회)
        Integer yesterdayDau = dauRepository.findByDate(today.minusDays(1).atStartOfDay())
                .map(DailyActiveUser::getCount)
                .orElse(0);

        // 7일 데이터 및 평균
        LocalDateTime start = sevenDaysAgo.atStartOfDay();
        LocalDateTime end = LocalDateTime.now();
        Double average = dauRepository.calculateAverageByDateRange(start, end);

        List<DailyDauDto> dailyData = dauRepository.findByDateBetweenOrderByDateDesc(start, end).stream()
                .map(dau -> DailyDauDto.builder()
                        .date(dau.getDate().toLocalDate().toString())
                        .count(dau.getCount())
                        .build())
                .collect(Collectors.toList());

        return DauResponseDto.builder()
//                .today(todayDau)
                .yesterday(yesterdayDau)
                .sevenDayAverage(average != null ? Math.round(average * 10.0) / 10.0 : 0.0)
                .dailyData(dailyData)
                .build();
    }

    @Override
    public ResourceUsageResponseDto getResourceUsage() {
        SystemMetric latestMetric = systemMetricRepository.findTopByOrderByTimestampDesc()
                .orElse(null);

        if (latestMetric != null) {
            return ResourceUsageResponseDto.builder()
                    .cpuUsage(latestMetric.getCpuUsage())
                    .memoryUsage(latestMetric.getMemoryUsage())
                    .storageUsage(latestMetric.getStorageUsage())
                    .timestamp(LocalDateTime.from(latestMetric.getTimestamp()))
                    .build();
        }

        return calculateCurrentResourceUsage();
    }

    @Override
    public SystemLogsResponseDto getRecentLogs(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<SystemLog> logs = systemLogRepository.findAllByOrderByTimestampDesc(pageable);

        List<SystemLogDto> logDtos = logs.stream()
                .map(log -> SystemLogDto.builder()
                        .id(log.getId())
                        .level(log.getLevel())
                        .message(log.getMessage())
                        .category(log.getCategory())
                        .timestamp(log.getTimestamp())
                        .build())
                .collect(Collectors.toList());

        return SystemLogsResponseDto.builder()
                .logs(logDtos)
                .totalCount(logDtos.size())
                .build();
    }

    @Override
    public DeploymentInfoResponseDto getDeploymentInfo() {
        DeploymentInfo deployment = deploymentInfoRepository
                .findTopByOrderByDeploymentTimeDesc()
                .orElse(createDefaultDeploymentInfo());

        // Duration 계산 시 타입을 맞춤 (LocalDateTime.now() 사용)
        Duration uptime = Duration.between(deployment.getServerStartTime(), LocalDateTime.now());
        String uptimeString = formatUptime(uptime);

        return DeploymentInfoResponseDto.builder()
                .version(deployment.getVersion())
                .environment(deployment.getEnvironment())
                .uptime(uptimeString)
                .lastDeployment(deployment.getDeploymentTime())
                .build();
    }

    // --- Private Helper Methods ---

    private ServerStatusDto checkServerStatus() {
        try {
            SystemMetric latestMetric = systemMetricRepository
                    .findTopByOrderByTimestampDesc()
                    .orElse(null);

            if (latestMetric == null) {
                return ServerStatusDto.builder().status("warning").message("No metrics available").build();
            }

            if (latestMetric.getCpuUsage() >= 90 || latestMetric.getMemoryUsage() >= 90 || latestMetric.getStorageUsage() >= 90) {
                return ServerStatusDto.builder().status("critical").message("High resource usage detected").build();
            }

            if (latestMetric.getCpuUsage() >= 70 || latestMetric.getMemoryUsage() >= 70 || latestMetric.getStorageUsage() >= 70) {
                return ServerStatusDto.builder().status("warning").message("Resource usage above normal").build();
            }

            return ServerStatusDto.builder().status("healthy").message("All systems operational").build();
        } catch (Exception e) {
            log.error("Error checking server status", e);
            return ServerStatusDto.builder().status("warning").message("Unable to determine status").build();
        }
    }

    private ResourceUsageResponseDto calculateCurrentResourceUsage() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            Runtime runtime = Runtime.getRuntime();

            double cpuUsage = osBean.getSystemLoadAverage() * 100.0;
            if (cpuUsage < 0) cpuUsage = 0.0;

            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            double memoryUsage = ((double)(totalMemory - freeMemory) / totalMemory) * 100.0;

            return ResourceUsageResponseDto.builder()
                    .cpuUsage(Math.round(cpuUsage * 100.0) / 100.0)
                    .memoryUsage(Math.round(memoryUsage * 100.0) / 100.0)
                    .storageUsage(0.0)
                    .timestamp(LocalDateTime.now()) // Instant 대신 LocalDateTime
                    .build();
        } catch (Exception e) {
            log.error("Error calculating resource usage", e);
            return ResourceUsageResponseDto.builder()
                    .cpuUsage(0.0).memoryUsage(0.0).storageUsage(0.0)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    private DeploymentInfo createDefaultDeploymentInfo() {
        return DeploymentInfo.builder()
                .version("v2.4.1")
                .environment("Production")
                .deploymentTime(LocalDateTime.now())
                .serverStartTime(LocalDateTime.now())
                .deployedBy("System")
                .build();
    }

    private String formatUptime(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        if (days > 0) return String.format("%d days %d hours", days, hours);
        else if (hours > 0) return String.format("%d hours %d minutes", hours, minutes);
        else return String.format("%d minutes", minutes);
    }
}