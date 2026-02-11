package com.aivle.ai0917.ipai.domain.manager.info.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "manager_notices", indexes = {
        @Index(name = "idx_manager_notices_manager_id", columnList = "managerId"),
        @Index(name = "idx_manager_notices_created_at", columnList = "createdAt")
})
public class ManagerNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 수신자: 매니저의 PK ID
    @Column(nullable = false, length = 8)
    private String managerId;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private boolean isRead = false;

    private String redirectUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public ManagerNotice(String managerId, String source, String title, String message, String redirectUrl) {
        this.managerId = managerId;
        this.source = source;
        this.title = title;
        this.message = message;
        this.redirectUrl = redirectUrl;
        this.createdAt = LocalDateTime.now();
    }

    public void markAsRead() {
        this.isRead = true;
    }
}