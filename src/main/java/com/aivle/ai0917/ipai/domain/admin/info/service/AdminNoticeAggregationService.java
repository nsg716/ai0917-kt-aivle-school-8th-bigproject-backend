package com.aivle.ai0917.ipai.domain.admin.info.service;

import com.aivle.ai0917.ipai.domain.admin.dashboard.model.DeploymentInfo;
import com.aivle.ai0917.ipai.domain.admin.dashboard.model.SystemLog;
import com.aivle.ai0917.ipai.domain.admin.dashboard.repository.DeploymentInfoRepository;
import com.aivle.ai0917.ipai.domain.admin.dashboard.repository.SystemLogRepository;
import com.aivle.ai0917.ipai.domain.admin.info.dto.UnifiedAdminNoticeDto;
import com.aivle.ai0917.ipai.domain.admin.info.dto.UnifiedAdminNoticeDto.NoticeSource;
import com.aivle.ai0917.ipai.domain.admin.info.dto.UnifiedAdminNoticeDto.NoticeSeverity;
import com.aivle.ai0917.ipai.domain.admin.info.model.AdminNotice;
import com.aivle.ai0917.ipai.domain.admin.info.repository.AdminNoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 통합 알림 집계 서비스
 * 모든 소스의 알림을 조회하여 통합
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminNoticeAggregationService {

    private final AdminNoticeRepository adminNoticeRepository;
    private final SystemLogRepository systemLogRepository;
    private final DeploymentInfoRepository deploymentInfoRepository;

    /**
     * admin_notices 테이블의 모든 데이터 조회 (디버깅/모니터링용)
     */
    @Transactional(readOnly = true)
    public List<UnifiedAdminNoticeDto> getAllAdminNotices() {
        try {
            log.info("Fetching all notices from admin_notices table");

            List<AdminNotice> allNotices = adminNoticeRepository.findAll();

            return allNotices.stream()
                    .map(this::convertAdminNoticeToDto)
                    .sorted() // 최신순 정렬
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching all admin notices", e);
            return new ArrayList<>();
        }
    }

    /**
     * 통합 알림 조회 (초기 로드용)
     *
     * @param hoursBack 조회 시간 범위
     * @param limit 최대 개수
     * @return 통합된 알림 목록
     */
    @Transactional(readOnly = true)
    public List<UnifiedAdminNoticeDto> getUnifiedNotices(int hoursBack, int limit) {
        LocalDateTime since = LocalDateTime.now().minusHours(hoursBack);
        List<UnifiedAdminNoticeDto> allNotices = new ArrayList<>();

        try {
            // 1. admin_notices 테이블에서 모든 알림 조회
            allNotices.addAll(collectFromAdminNotices(since));
            log.debug("Collected {} notices from admin_notices", allNotices.size());

//            // 2. system_logs 테이블에서 ERROR/WARNING 조회
//            allNotices.addAll(collectFromSystemLogs(since));
//            log.debug("Total notices after system_logs: {}", allNotices.size());
//
//            // 3. deployment_info 테이블에서 배포 정보 조회
//            allNotices.addAll(collectFromDeploymentInfo(since));
//            log.debug("Total notices after deployment_info: {}", allNotices.size());

        } catch (Exception e) {
            log.error("Error collecting unified notices", e);
        }

        // 최신순 정렬 후 limit 적용
        return allNotices.stream()
                .sorted()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * admin_notices 테이블에서 조회
     * (시스템 메트릭 + 관리자 커스텀 알림 포함)
     */
    private List<UnifiedAdminNoticeDto> collectFromAdminNotices(LocalDateTime since) {
        try {
            return adminNoticeRepository
                    .findByCreatedAtAfterOrderByCreatedAtDesc(since)
                    .stream()
                    .map(this::convertAdminNoticeToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error collecting from admin_notices", e);
            return new ArrayList<>();
        }
    }

//    /**
//     * system_logs 테이블에서 조회
//     */
//    private List<UnifiedAdminNoticeDto> collectFromSystemLogs(LocalDateTime since) {
//        try {
//            return systemLogRepository
//                    .findRecentCriticalLogs(since)
//                    .stream()
//                    .map(this::convertSystemLogToDto)
//                    .collect(Collectors.toList());
//        } catch (Exception e) {
//            log.error("Error collecting from system_logs", e);
//            return new ArrayList<>();
//        }
//    }
//
//    /**
//     * deployment_info 테이블에서 조회
//     */
//    private List<UnifiedAdminNoticeDto> collectFromDeploymentInfo(LocalDateTime since) {
//        try {
//            return deploymentInfoRepository
//                    .findByDeploymentTimeAfterOrderByDeploymentTimeDesc(since)
//                    .stream()
//                    .map(this::convertDeploymentToDto)
//                    .collect(Collectors.toList());
//        } catch (Exception e) {
//            log.error("Error collecting from deployment_info", e);
//            return new ArrayList<>();
//        }
//    }

    /**
     * AdminNotice -> UnifiedAdminNoticeDto 변환
     */
    private UnifiedAdminNoticeDto convertAdminNoticeToDto(AdminNotice notice) {
        return UnifiedAdminNoticeDto.builder()
                .id(notice.getId())
                .source(NoticeSource.valueOf(notice.getSource()))
                .category(notice.getCategory())
                .title(buildTitle(notice))
                .message(notice.getMessage())
                .severity(NoticeSeverity.valueOf(notice.getSeverity()))
                .isRead(notice.isRead())
                .createdAt(notice.getCreatedAt())
                .build();
    }

    /**
     * SystemLog -> UnifiedAdminNoticeDto 변환
     */
    private UnifiedAdminNoticeDto convertSystemLogToDto(SystemLog log) {
        return UnifiedAdminNoticeDto.builder()
                .id(log.getId())
                .source(NoticeSource.SYSTEM_LOG)
                .category(log.getCategory())
                .title(generateLogTitle(log))
                .message(truncate(log.getMessage(), 150))
                .severity(mapLogLevelToSeverity(log.getLevel()))
                .isRead(Boolean.TRUE.equals(log.getIsRead()))
                .createdAt(log.getTimestamp())
                .build();
    }

    /**
     * DeploymentInfo -> UnifiedAdminNoticeDto 변환
     */
    private UnifiedAdminNoticeDto convertDeploymentToDto(DeploymentInfo deployment) {
        return UnifiedAdminNoticeDto.builder()
                .id(deployment.getId())
                .source(NoticeSource.DEPLOYMENT)
                .category("DEPLOYMENT_" + deployment.getStatusOrDefault())
                .title("🚀 배포: " + deployment.getVersion() + " (" + deployment.getEnvironment() + ")")
                .message(truncate(deployment.getDescriptionOrDefault(), 150))
                .severity(mapDeploymentStatusToSeverity(deployment.getStatusOrDefault()))
                .isRead(Boolean.TRUE.equals(deployment.getIsRead()))
                .createdAt(deployment.getDeploymentTime())
                .build();
    }

    /**
     * AdminNotice 제목 생성
     */
    private String buildTitle(AdminNotice notice) {
        NoticeSource source = NoticeSource.valueOf(notice.getSource());
        NoticeSeverity severity = NoticeSeverity.valueOf(notice.getSeverity());

        return switch (source) {
            case SYSTEM_METRIC -> severity.getIcon() + " 시스템 리소스: " + notice.getCategory();
            case ADMIN_CUSTOM -> "📢 " + notice.getCategory();
            default -> notice.getCategory();
        };
    }

    /**
     * 시스템 로그 제목 생성
     */
    private String generateLogTitle(SystemLog log) {
        String icon = log.getLevel().equals("ERROR") ? "🔴" : "⚠️";
        String prefix = log.getLevel().equals("ERROR") ? "에러" : "경고";

        return switch (log.getCategory()) {
            case "DB_BACKUP" -> icon + " " + prefix + ": 데이터베이스 백업";
            case "API_DELAY" -> icon + " " + prefix + ": API 지연";
            case "DB_CONNECTION" -> icon + " " + prefix + ": DB 연결";
            case "RESOURCE_CRITICAL" -> icon + " " + prefix + ": 리소스 임계치";
            default -> icon + " " + prefix + ": " + log.getCategory();
        };
    }

    /**
     * 로그 레벨 -> 심각도 매핑
     */
    private NoticeSeverity mapLogLevelToSeverity(String level) {
        return switch (level.toUpperCase()) {
            case "ERROR" -> NoticeSeverity.ERROR;
            case "WARN", "WARNING" -> NoticeSeverity.WARNING;
            default -> NoticeSeverity.INFO;
        };
    }

    /**
     * 배포 상태 -> 심각도 매핑
     */
    private NoticeSeverity mapDeploymentStatusToSeverity(String status) {
        return switch (status.toUpperCase()) {
            case "FAILED" -> NoticeSeverity.ERROR;
            case "IN_PROGRESS" -> NoticeSeverity.WARNING;
            default -> NoticeSeverity.INFO;
        };
    }

    /**
     * 텍스트 자르기
     */
    private String truncate(String text, int maxLength) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.length() > maxLength
                ? text.substring(0, maxLength) + "..."
                : text;
    }

    @Transactional(readOnly = true)
    public List<UnifiedAdminNoticeDto> getUnreadAdminNotices() {
        return adminNoticeRepository.findByIsReadFalseOrderByCreatedAtDesc()
                .stream()
                .map(this::convertAdminNoticeToDto)
                .collect(Collectors.toList());
    }
}