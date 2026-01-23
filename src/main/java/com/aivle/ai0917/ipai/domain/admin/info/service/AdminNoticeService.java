package com.aivle.ai0917.ipai.domain.admin.info.service;

import com.aivle.ai0917.ipai.domain.admin.dashboard.model.DeploymentInfo;
import com.aivle.ai0917.ipai.domain.admin.dashboard.model.SystemLog;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AdminNoticeService {

    /**
     * SSE 구독
     * @param adminId 관리자 ID
     * @return SSE Emitter
     */
    SseEmitter subscribe(Long adminId);

    /**
     * 시스템 메트릭 임계치 초과 알림
     * @param category 카테고리 (예: RESOURCE_CRITICAL)
     * @param message 메시지
     * @param metadata 메타데이터 (JSON 형식)
     */
    void sendSystemMetricAlert(String category, String message, String metadata);

    /**
     * 시스템 로그 알림
     * @param systemLog 시스템 로그 엔티티
     */
    void sendSystemLogAlert(SystemLog systemLog);

    /**
     * 배포 정보 알림
     * @param deployment 배포 정보 엔티티
     */
    void sendDeploymentAlert(DeploymentInfo deployment);

    /**
     * 관리자 커스텀 알림
     * @param category 카테고리
     * @param message 메시지
     * @param role 대상 역할
     */
    void sendCustomAlert(String category, String message, String role);

    /**
     * 알림 읽음 처리
     * @param source 알림 소스 (SYSTEM_METRIC, SYSTEM_LOG, DEPLOYMENT, ADMIN_CUSTOM)
     * @param id 알림 ID
     */
    void markAsRead(String source, Long id);

    /**
     *
     *
     */
    void markAllAsRead();
}