package com.aivle.ai0917.ipai.domain.admin.dashboard.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


// 배포 정보 엔티티
@Entity
@Table(name = "deployment_info")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeploymentInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String version;

    @Column(nullable = false, length = 20)
    private String environment;

    @Column(nullable = false)
    private LocalDateTime deploymentTime;

    @Column(nullable = false)
    private LocalDateTime serverStartTime;

    @Column
    private String deployedBy;

    @Column(columnDefinition = "TEXT")
    private String releaseNotes;

    // ========== 알림 시스템을 위한 추가 컬럼 (기존 기능 영향 없음) ==========
    @Builder.Default
    @Column(nullable = false)
    private Boolean isRead = false; // Boolean으로 변경


    @Column(length = 20)
    private String status; // SUCCESS, FAILED, IN_PROGRESS (nullable로 기존 데이터 호환)

    @Column(columnDefinition = "TEXT")
    private String description; // 배포 설명 (nullable로 기존 데이터 호환)

    // ========== 기존 데이터 호환을 위한 헬퍼 메서드 ==========

    /**
     * 알림 시스템용 status 값 반환 (null이면 기본값 SUCCESS)
     */
    public String getStatusOrDefault() {
        return status != null ? status : "SUCCESS";
    }

    /**
     * 알림 시스템용 description 반환 (null이면 releaseNotes 사용)
     */
    public String getDescriptionOrDefault() {
        return description != null ? description : releaseNotes;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
