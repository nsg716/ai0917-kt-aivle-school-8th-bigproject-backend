package com.aivle.ai0917.ipai.domain.admin.info.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 통합 관리자 알림 DTO
 * - 시스템 메트릭 임계치 초과 알림
 * - 시스템 로그 (ERROR/WARNING)
 * - 배포 정보
 * - 관리자 커스텀 알림
 */
@Getter
@Builder
public class UnifiedAdminNoticeDto implements Comparable<UnifiedAdminNoticeDto> {

    /**
     * 알림 고유 ID (소스별 ID)
     */
    private Long id;

    /**
     * 알림 소스 타입
     * - SYSTEM_METRIC: 시스템 리소스 임계치 초과
     * - SYSTEM_LOG: 시스템 에러/경고 로그
     * - DEPLOYMENT: 배포 관련 알림
     * - ADMIN_CUSTOM: 관리자가 직접 생성한 알림
     */
    private NoticeSource source;

    /**
     * 세부 카테고리
     * 예) RESOURCE_CRITICAL, DB_BACKUP, DEPLOYMENT_SUCCESS 등
     */
    private String category;

    /**
     * 알림 제목
     */
    private String title;

    /**
     * 알림 메시지 본문
     */
    private String message;

    /**
     * 심각도 레벨
     */
    private NoticeSeverity severity;

    /**
     * 읽음 여부
     */
    private boolean isRead;

    /**
     * 알림 발생 시간
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;


    /**
     * 최신순 정렬 (createdAt 기준)
     */
    @Override
    public int compareTo(UnifiedAdminNoticeDto o) {
        return o.createdAt.compareTo(this.createdAt);
    }

    /**
     * 심각도 기반 정렬 우선순위
     */
    public int getSeverityPriority() {
        return severity.getPriority();
    }

    /**
     * 알림 소스 타입
     */
    public enum NoticeSource {
        SYSTEM_METRIC("시스템 메트릭"),
        SYSTEM_LOG("시스템 로그"),
        DEPLOYMENT("배포 정보"),
        ADMIN_CUSTOM("관리자 알림");

        private final String description;

        NoticeSource(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 심각도 레벨
     */
    public enum NoticeSeverity {
        CRITICAL(4, "🔴"),
        ERROR(3, "❌"),
        WARNING(2, "⚠️"),
        INFO(1, "ℹ️");

        private final int priority;
        private final String icon;

        NoticeSeverity(int priority, String icon) {
            this.priority = priority;
            this.icon = icon;
        }

        public int getPriority() {
            return priority;
        }

        public String getIcon() {
            return icon;
        }
    }
}