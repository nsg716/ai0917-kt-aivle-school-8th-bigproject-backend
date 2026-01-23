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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminNoticeServiceImpl implements AdminNoticeService {

    private final AdminNoticeRepository adminNoticeRepository;
    private final SystemLogRepository systemLogRepository;
    private final DeploymentInfoRepository deploymentInfoRepository;

    // SSE 연결 관리
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private static final long SSE_TIMEOUT = 60 * 60 * 1000L; // 1시간

    /**
     * SSE 구독 (관리자별)
     */
    @Override
    public SseEmitter subscribe(Long adminId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.put(adminId, emitter);

        emitter.onCompletion(() -> {
            log.info("SSE completed for admin: {}", adminId);
            emitters.remove(adminId);
        });

        emitter.onTimeout(() -> {
            log.warn("SSE timeout for admin: {}", adminId);
            emitters.remove(adminId);
        });

        emitter.onError((ex) -> {
            log.error("SSE error for admin: {}", adminId, ex);
            emitters.remove(adminId);
        });

        // 연결 성공 메시지 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("SSE 연결 성공"));
        } catch (IOException e) {
            log.error("Failed to send connection message", e);
            emitters.remove(adminId);
        }

        log.info("Admin {} subscribed to SSE notifications. Active connections: {}",
                adminId, emitters.size());

        return emitter;
    }

    /**
     * 시스템 메트릭 임계치 초과 알림
     * - admin_notices DB에 저장
     * - SSE로 실시간 전송
     */
    @Override
    @Transactional
    public void sendSystemMetricAlert(String category, String message, String metadata) {
        // 1. DB 저장
        AdminNotice notice = AdminNotice.builder()
                .source(NoticeSource.SYSTEM_METRIC.name())
                .category(category)
                .message(message)
                .severity(NoticeSeverity.WARNING.name())
                .targetRole("Admin")
                .metadata(metadata)
                .build();

        AdminNotice saved = adminNoticeRepository.save(notice);
        log.info("System metric alert saved: id={}, category={}", saved.getId(), category);

        // 2. DTO 변환
        UnifiedAdminNoticeDto dto = convertToDto(saved);

        // 3. SSE 실시간 전송
        broadcastToAllAdmins(dto);
    }

    /**
     * 시스템 로그 알림 (ERROR/WARNING)
     * - system_logs DB에 이미 저장됨
     * - admin_notices에도 복사 저장
     * - SSE로 실시간 전송
     */
    @Override
    @Transactional
    public void sendSystemLogAlert(SystemLog systemLog) {
        // 1. admin_notices에 복사 저장
        AdminNotice notice = AdminNotice.builder()
                .source(NoticeSource.SYSTEM_LOG.name())
                .category(systemLog.getCategory())
                .message(systemLog.getMessage())
                .severity(mapLogLevelToSeverity(systemLog.getLevel()))
                .targetRole("Admin")
                .relatedEntity("SYSTEM_LOG:" + systemLog.getId())
                .metadata(systemLog.getMetadata())
                .build();

        AdminNotice saved = adminNoticeRepository.save(notice);
        log.info("System log alert saved: id={}, category={}", saved.getId(), systemLog.getCategory());

        // 2. DTO 변환 (원본 SystemLog 기반)
        UnifiedAdminNoticeDto dto = UnifiedAdminNoticeDto.builder()
                .id(systemLog.getId())
                .source(NoticeSource.SYSTEM_LOG)
                .category(systemLog.getCategory())
                .title(generateLogTitle(systemLog))
                .message(truncate(systemLog.getMessage(), 150))
                .severity(NoticeSeverity.valueOf(mapLogLevelToSeverity(systemLog.getLevel())))
                .isRead(Boolean.TRUE.equals(systemLog.getIsRead()))
                .createdAt(systemLog.getTimestamp())
                .build();

        // 3. SSE 실시간 전송
        broadcastToAllAdmins(dto);
    }

    /**
     * 배포 정보 알림
     * - deployment_info DB에 이미 저장됨
     * - admin_notices에도 복사 저장
     * - SSE로 실시간 전송
     */
    @Override
    @Transactional
    public void sendDeploymentAlert(DeploymentInfo deployment) {
        // 1. admin_notices에 복사 저장
        AdminNotice notice = AdminNotice.builder()
                .source(NoticeSource.DEPLOYMENT.name())
                .category("DEPLOYMENT_" + deployment.getStatusOrDefault())
                .message(deployment.getDescriptionOrDefault())
                .severity(mapDeploymentStatusToSeverity(deployment.getStatusOrDefault()))
                .targetRole("Admin")
                .relatedEntity("DEPLOYMENT:" + deployment.getId())
                .build();

        AdminNotice saved = adminNoticeRepository.save(notice);
        log.info("Deployment alert saved: id={}, version={}", saved.getId(), deployment.getVersion());

        // 2. DTO 변환 (원본 DeploymentInfo 기반)
        UnifiedAdminNoticeDto dto = UnifiedAdminNoticeDto.builder()
                .id(deployment.getId())
                .source(NoticeSource.DEPLOYMENT)
                .category("DEPLOYMENT_" + deployment.getStatusOrDefault())
                .title("🚀 배포: " + deployment.getVersion() + " (" + deployment.getEnvironment() + ")")
                .message(truncate(deployment.getDescriptionOrDefault(), 150))
                .severity(NoticeSeverity.valueOf(mapDeploymentStatusToSeverity(deployment.getStatusOrDefault())))
                .isRead(Boolean.TRUE.equals(deployment.getIsRead()))
                .createdAt(deployment.getDeploymentTime())
                .build();

        // 3. SSE 실시간 전송
        broadcastToAllAdmins(dto);
    }

    /**
     * 관리자 커스텀 알림
     * - admin_notices DB에 저장
     * - SSE로 실시간 전송
     */
    @Override
    @Transactional
    public void sendCustomAlert(String category, String message, String role) {
        // 1. DB 저장
        AdminNotice notice = AdminNotice.builder()
                .source(NoticeSource.ADMIN_CUSTOM.name())
                .category(category)
                .message(message)
                .severity(NoticeSeverity.INFO.name())
                .targetRole(role)
                .build();

        AdminNotice saved = adminNoticeRepository.save(notice);
        log.info("Custom alert saved: id={}, category={}", saved.getId(), category);

        // 2. DTO 변환
        UnifiedAdminNoticeDto dto = convertToDto(saved);

        // 3. SSE 실시간 전송
        broadcastToAllAdmins(dto);
    }

    /**
     * 알림 읽음 처리
     */
    @Override
    @Transactional
    public void markAsRead(String source, Long id) {
        try {
            NoticeSource sourceType = NoticeSource.valueOf(source);

            switch (sourceType) {
                case ADMIN_CUSTOM, SYSTEM_METRIC ->
                        adminNoticeRepository.findById(id)
                                .ifPresent(AdminNotice::markAsRead);

                case SYSTEM_LOG ->
                        systemLogRepository.findById(id)
                                .ifPresent(SystemLog::markAsRead);

                case DEPLOYMENT ->
                        deploymentInfoRepository.findById(id)
                                .ifPresent(DeploymentInfo::markAsRead);
            }

            log.info("Marked as read: source={}, id={}", source, id);

        } catch (IllegalArgumentException e) {
            log.error("Unknown notice source: {}", source);
            throw new IllegalArgumentException("Unknown source: " + source);
        }
    }

    /**
     * SSE로 모든 연결된 관리자에게 브로드캐스트
     */
    private void broadcastToAllAdmins(UnifiedAdminNoticeDto dto) {
        emitters.forEach((adminId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("admin-notice")
                        .data(dto));

                log.debug("Notification sent to admin: {}", adminId);

            } catch (IOException e) {
                log.error("Failed to send notification to admin: {}", adminId, e);
                emitters.remove(adminId);
            }
        });

        log.info("Broadcasted notification to {} admins. Source: {}, Category: {}",
                emitters.size(), dto.getSource(), dto.getCategory());
    }

    /**
     * AdminNotice -> UnifiedAdminNoticeDto 변환
     */
    private UnifiedAdminNoticeDto convertToDto(AdminNotice notice) {
        return UnifiedAdminNoticeDto.builder()
                .id(notice.getId())
                .source(NoticeSource.valueOf(notice.getSource()))
                .category(notice.getCategory())
                .title(notice.getCategory())
                .message(notice.getMessage())
                .severity(NoticeSeverity.valueOf(notice.getSeverity()))
                .isRead(notice.isRead())
                .createdAt(notice.getCreatedAt())
                .build();
    }

    /**
     * 로그 레벨 -> 심각도 매핑
     */
    private String mapLogLevelToSeverity(String level) {
        return switch (level.toUpperCase()) {
            case "ERROR" -> NoticeSeverity.ERROR.name();
            case "WARN", "WARNING" -> NoticeSeverity.WARNING.name();
            default -> NoticeSeverity.INFO.name();
        };
    }

    /**
     * 배포 상태 -> 심각도 매핑
     */
    private String mapDeploymentStatusToSeverity(String status) {
        return switch (status.toUpperCase()) {
            case "FAILED" -> NoticeSeverity.ERROR.name();
            case "IN_PROGRESS" -> NoticeSeverity.WARNING.name();
            default -> NoticeSeverity.INFO.name();
        };
    }

    /**
     * 시스템 로그 제목 생성
     */
    private String generateLogTitle(SystemLog log) {
        String prefix = log.getLevel().equals("ERROR") ? "🔴 에러" : "⚠️ 경고";
        return switch (log.getCategory()) {
            case "DB_BACKUP" -> prefix + ": 데이터베이스 백업";
            case "API_DELAY" -> prefix + ": API 지연";
            case "DB_CONNECTION" -> prefix + ": DB 연결";
            case "RESOURCE_CRITICAL" -> prefix + ": 리소스 임계치 초과";
            default -> prefix + ": " + log.getCategory();
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

    @Override
    @Transactional
    public void markAllAsRead() {
        // 1. 통합 알림 테이블(admin_notices) 업데이트
        adminNoticeRepository.markAllAsRead();

        // 2. 시스템 로그 테이블 업데이트 (읽지 않은 것만 찾아 markAsRead 실행 또는 벌크 업데이트)
        systemLogRepository.findAll().stream()
                .filter(log -> !Boolean.TRUE.equals(log.getIsRead()))
                .forEach(SystemLog::markAsRead);

        // 3. 배포 정보 테이블 업데이트
        deploymentInfoRepository.findAll().stream()
                .filter(deploy -> !Boolean.TRUE.equals(deploy.getIsRead()))
                .forEach(DeploymentInfo::markAsRead);

        log.info("All notifications have been marked as read.");
    }
}