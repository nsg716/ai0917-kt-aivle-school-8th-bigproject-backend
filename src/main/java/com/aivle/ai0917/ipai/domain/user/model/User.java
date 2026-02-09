package com.aivle.ai0917.ipai.domain.user.model;

import com.aivle.ai0917.ipai.domain.admin.access.model.UserRole;
import com.aivle.ai0917.ipai.global.utils.Base62Util; // 앞서 만든 유틸리티
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users", indexes = {
        @Index(name = "idx_users_naver_id", columnList = "naver_id", unique = true),
        @Index(name = "idx_users_email_id", columnList = "site_email", unique = true),
        @Index(name = "idx_users_integration_id", columnList = "integration_id", unique = true) // 인덱스 추가
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 외부 시스템 연동용 고유 8자리 ID */
    @Column(name = "integration_id", unique = true, nullable = false, length = 8)
    private String integrationId;

    @Column(unique = true, length = 64)
    private String naverId;

    @Column(name = "site_email", unique = true, length = 50, nullable = true)
    private String siteEmail;

    @Column(name = "site_pwd", nullable = true)
    private String sitePwd;

    private String email;
    private String name;
    private String gender;
    private String birthYear;
    private String birthday;
    private String mobile;

    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.Author;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "refresh_token_hash")
    private String refreshTokenHash;

    @Column(name = "manager_integration_id", nullable =true)
    private String managerIntegrationId;


    @Builder
    public User(String naverId, String siteEmail, String sitePwd, String email, String name,
                String gender, String birthYear, String birthday, String mobile, UserRole role,
                LocalDateTime createdAt, LocalDateTime updatedAt, String integrationId, String managerIntegrationId) {
        this.naverId = naverId;
        this.siteEmail = siteEmail;
        this.sitePwd = sitePwd;
        this.email = email;
        this.name = name;
        this.gender = gender;
        this.birthYear = birthYear;
        this.birthday = birthday;
        this.mobile = mobile;
        this.role = (role != null) ? role : UserRole.Author;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.integrationId = integrationId;
        this.managerIntegrationId = managerIntegrationId;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        // 저장 시 값이 없으면 8자리 고유값 생성
        if (this.integrationId == null) {
            this.integrationId = Base62Util.generate8CharId();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateAccess(UserRole role) {
        this.role = role;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }
}