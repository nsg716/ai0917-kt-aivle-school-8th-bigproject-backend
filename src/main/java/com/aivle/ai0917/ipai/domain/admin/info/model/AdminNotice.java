package com.aivle.ai0917.ipai.domain.admin.info.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 관리자 알림 통합 테이블
 * 모든 종류의 알림을 저장
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "admin_notices", indexes = {
        @Index(name = "idx_admin_notices_created_at", columnList = "createdAt"),
        @Index(name = "idx_admin_notices_source_category", columnList = "source, category"),
        @Index(name = "idx_admin_notices_is_read", columnList = "isRead"),
        @Index(name = "idx_admin_notices_target_role", columnList = "targetRole")
})
public class AdminNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 알림 소스
     * SYSTEM_METRIC, SYSTEM_LOG, DEPLOYMENT, ADMIN_CUSTOM
     */
    @Column(nullable = false, length = 30)
    private String source;

    /**
     * 세부 카테고리
     * 예) RESOURCE_CRITICAL, DB_BACKUP, DEPLOYMENT_SUCCESS
     */
    @Column(nullable = false, length = 50)
    private String category;

    /**
     * 알림 메시지
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * 심각도
     * CRITICAL, ERROR, WARNING, INFO
     */
    @Column(nullable = false, length = 20)
    private String severity;

    /**
     * 읽음 여부
     */
    @Column(nullable = false, name = "isRead")
    private boolean isRead = false;

    /**
     * 대상 역할 (선택)
     * Admin, SuperAdmin 등
     */
    @Column(length = 30, name = "targetRole")
    private String targetRole;

    /**
     * 관련 엔티티 (선택)
     * 예) SYSTEM_LOG:123, DEPLOYMENT:456
     */
    @Column(length = 100, name = "relatedEntity")
    private String relatedEntity;

    /**
     * 클릭 시 이동할 URL (선택)
     */
    @Column(length = 200, name = "actionUrl")
    private String actionUrl;

    /**
     * 추가 메타데이터 (JSON 형식, 선택)
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    /**
     * 생성 시간
     */
    @Column(nullable = false, name = "createdAt")
    private LocalDateTime createdAt;

    @Builder
    public AdminNotice(String source, String category, String message, String severity,
                       String targetRole, String relatedEntity, String actionUrl, String metadata) {
        this.source = source;
        this.category = category;
        this.message = message;
        this.severity = severity;
        this.targetRole = targetRole;
        this.relatedEntity = relatedEntity;
        this.actionUrl = actionUrl;
        this.metadata = metadata;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 읽음 처리
     */
    public void markAsRead() {
        this.isRead = true;
    }
}