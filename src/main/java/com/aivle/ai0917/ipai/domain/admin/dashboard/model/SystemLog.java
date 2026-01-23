
package com.aivle.ai0917.ipai.domain.admin.dashboard.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


// 시스템 로그 엔티티
@Entity
@Table(name = "system_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String level; // INFO, WARNING, ERROR

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false, length = 50)
    private String category; // DB_BACKUP, ACCOUNT_CREATED, API_DELAY

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String metadata; // JSON 형태의 추가 정보

    // ========== 알림 시스템을 위한 추가 컬럼 (기존 기능 영향 없음) ==========

    @Column(columnDefinition = "TEXT")
    private String stackTrace; // 에러 스택 트레이스 (nullable로 기존 데이터 호환)

    @Builder.Default
    @Column(nullable = false)
    private Boolean isRead = false; // boolean -> Boolean으로 변경


    // ========== 헬퍼 메서드 ==========

    /**
     * 알림에 표시할지 여부 판단 (ERROR, WARNING만 알림 대상)
     */
    public boolean isNotificationWorthy() {
        return "ERROR".equals(level) || "WARNING".equals(level);
    }

    /**
     * 심각도 레벨 반환 (알림 시스템용)
     */
    public String getSeverityLevel() {
        return switch (level) {
            case "ERROR" -> "CRITICAL";
            case "WARNING" -> "WARNING";
            default -> "INFO";
        };
    }

    public void markAsRead() {
        this.isRead = true;
    }
}