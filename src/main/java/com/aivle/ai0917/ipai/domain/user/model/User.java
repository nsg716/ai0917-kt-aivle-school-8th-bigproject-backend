package com.aivle.ai0917.ipai.domain.user.model;

import com.aivle.ai0917.ipai.domain.admin.access.model.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users", indexes = {
        @Index(name = "idx_users_naver_id", columnList = "naver_id", unique = true),
        @Index(name = "idx_users_email_id", columnList = "site_email", unique = true)
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 네이버 고유 ID (소셜 로그인 사용자는 필수, 일반 사용자는 null) */
    @Column(unique = true, length = 64)
    private String naverId;

    /** 사이트 자체 로그인 아이디 (일반 사용자는 필수, 소셜 사용자는 null 허용) */
    @Column(name = "site_email", unique = true, length = 50, nullable = true)
    private String siteEmail;

    /** 사이트 자체 로그인 비밀번호 (소셜 사용자는 null 허용) */
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

    @Builder
    public User(String naverId, String siteEmail, String sitePwd, String email, String name,
                String gender, String birthYear, String birthday, String mobile, UserRole role, LocalDateTime createdAt, LocalDateTime updatedAt ) {
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
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 비즈니스 로직: 권한 수정
    public void updateAccess(UserRole role) {
        this.role = role;
        this.updatedAt = LocalDateTime.now();
    }
}