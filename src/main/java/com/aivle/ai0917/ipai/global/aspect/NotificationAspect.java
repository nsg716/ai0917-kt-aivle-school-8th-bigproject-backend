package com.aivle.ai0917.ipai.global.aspect;

import com.aivle.ai0917.ipai.domain.admin.dashboard.model.DeploymentInfo;
import com.aivle.ai0917.ipai.domain.admin.dashboard.model.SystemLog;
import com.aivle.ai0917.ipai.domain.admin.info.service.AdminNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 시스템 로그 및 배포 정보 저장 시 자동으로 알림을 전송하는 AOP
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class NotificationAspect {

    private final AdminNoticeService adminNoticeService;

    /**
     * SystemLog 저장 후 자동 알림 전송
     * - ERROR, WARNING 레벨만 알림 전송
     */
    @AfterReturning(
            pointcut = "execution(* com.aivle.ai0917.ipai.domain.admin.dashboard.repository.SystemLogRepository.save(..))",
            returning = "savedLog"
    )
    public void sendNotificationAfterSystemLogSave(Object savedLog) {
        if (!(savedLog instanceof SystemLog)) {
            return;
        }

        SystemLog systemLog = (SystemLog) savedLog;

        try {
            // 리소스 임계치 알림은 SYSTEM_METRIC에서 처리하므로 로그 기반 알림 전송에서 제외
            if ("RESOURCE_CRITICAL".equals(systemLog.getCategory())) {
                log.debug("[Notification AOP] RESOURCE_CRITICAL 로그는 SYSTEM_METRIC에서 처리하므로 건너뜁니다.");
                return;
            }

            // ERROR 또는 WARNING 레벨만 알림 전송 (나머지 카테고리: DB_BACKUP, API_DELAY 등은 전송)
            if ("ERROR".equals(systemLog.getLevel()) || "WARNING".equals(systemLog.getLevel())) {
                adminNoticeService.sendSystemLogAlert(systemLog);
            }
        } catch (Exception e) {
            log.error("[Notification AOP] 알림 전송 실패", e);
        }
    }

    /**
     * DeploymentInfo 저장 후 자동 알림 전송
     * - 모든 배포 정보에 대해 알림 전송
     */
    @AfterReturning(
            pointcut = "execution(* com.aivle.ai0917.ipai.domain.admin.dashboard.repository.DeploymentInfoRepository.save(..))",
            returning = "savedDeployment"
    )
    public void sendNotificationAfterDeploymentSave(Object savedDeployment) {
        if (!(savedDeployment instanceof DeploymentInfo)) {
            return;
        }

        DeploymentInfo deployment = (DeploymentInfo) savedDeployment;

        try {
            log.info("[Notification AOP] DeploymentInfo 저장 감지 - ID: {}, Version: {}, Status: {}",
                    deployment.getId(), deployment.getVersion(), deployment.getStatusOrDefault());

            adminNoticeService.sendDeploymentAlert(deployment);

            log.info("[Notification AOP] DeploymentInfo 알림 전송 완료");

        } catch (Exception e) {
            log.error("[Notification AOP] DeploymentInfo 알림 전송 실패", e);
        }
    }

    /**
     * SystemLog의 saveAll() 메서드도 지원
     */
    @AfterReturning(
            pointcut = "execution(* com.aivle.ai0917.ipai.domain.admin.dashboard.repository.SystemLogRepository.saveAll(..))",
            returning = "savedLogs"
    )
    public void sendNotificationAfterSystemLogSaveAll(Object savedLogs) {
        if (!(savedLogs instanceof Iterable)) {
            return;
        }

        try {
            @SuppressWarnings("unchecked")
            Iterable<SystemLog> logs = (Iterable<SystemLog>) savedLogs;

            for (SystemLog systemLog : logs) {
                if ("ERROR".equals(systemLog.getLevel()) || "WARNING".equals(systemLog.getLevel())) {
                    log.info("[Notification AOP] SystemLog 일괄 저장 감지 - ID: {}, Level: {}",
                            systemLog.getId(), systemLog.getLevel());

                    adminNoticeService.sendSystemLogAlert(systemLog);
                }
            }

            log.info("[Notification AOP] SystemLog 일괄 알림 전송 완료");

        } catch (Exception e) {
            log.error("[Notification AOP] SystemLog 일괄 알림 전송 실패", e);
        }
    }

    /**
     * DeploymentInfo의 saveAll() 메서드도 지원
     */
    @AfterReturning(
            pointcut = "execution(* com.aivle.ai0917.ipai.domain.admin.dashboard.repository.DeploymentInfoRepository.saveAll(..))",
            returning = "savedDeployments"
    )
    public void sendNotificationAfterDeploymentSaveAll(Object savedDeployments) {
        if (!(savedDeployments instanceof Iterable)) {
            return;
        }

        try {
            @SuppressWarnings("unchecked")
            Iterable<DeploymentInfo> deployments = (Iterable<DeploymentInfo>) savedDeployments;

            for (DeploymentInfo deployment : deployments) {
                log.info("[Notification AOP] DeploymentInfo 일괄 저장 감지 - ID: {}, Version: {}",
                        deployment.getId(), deployment.getVersion());

                adminNoticeService.sendDeploymentAlert(deployment);
            }

            log.info("[Notification AOP] DeploymentInfo 일괄 알림 전송 완료");

        } catch (Exception e) {
            log.error("[Notification AOP] DeploymentInfo 일괄 알림 전송 실패", e);
        }
    }
}