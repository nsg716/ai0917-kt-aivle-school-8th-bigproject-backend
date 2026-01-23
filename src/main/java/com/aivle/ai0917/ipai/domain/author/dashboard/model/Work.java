package com.aivle.ai0917.ipai.domain.author.dashboard.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
@Entity
@Table(name = "works")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Work {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    // users 테이블의 id (PK)와 연결되는 외래키 역할
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // users 테이블의 name 값을 저장 (요청하신 writer 필드)
    @Column(nullable = false)
    private String writer;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkStatus status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Work(String title, Long userId, String writer, String description, WorkStatus status) {
        this.title = title;
        this.userId = userId;
        this.writer = writer;
        this.description = description;
        this.status = status;
    }
}