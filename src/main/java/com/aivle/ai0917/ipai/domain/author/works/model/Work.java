//package com.aivle.ai0917.ipai.domain.author.works.model;
//
//import jakarta.persistence.*;
//import lombok.*;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.annotation.LastModifiedDate;
//import org.springframework.data.jpa.domain.support.AuditingEntityListener;
//
//import java.time.LocalDateTime;
//@Entity
//@Table(name = "works")
//@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@EntityListeners(AuditingEntityListener.class)
//public class Work {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private String title;
//
//    // users 테이블의 id (PK)와 연결되는 외래키 역할
//    @Column(name = "user_integration_id", nullable = false, length = 8)
//    private String userIntegrationId;
//
//    // users 테이블의 name 값을 저장 (요청하신 writer 필드)
//    @Column(nullable = false)
//    private String writer;
//
//    @Column(columnDefinition = "TEXT")
//    private String description;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private WorkStatus status;
//
//    @CreatedDate
//    @Column(updatable = false)
//    private LocalDateTime createdAt;
//
//    @LastModifiedDate
//    private LocalDateTime updatedAt;
//
//    @Builder
//    public Work(String title, String userIntegrationId, String writer, String description, WorkStatus status) {
//        this.title = title;
//        this.userIntegrationId = userIntegrationId;
//        this.writer = writer;
//        this.description = description;
//        this.status = status;
//    }
//    public void update(String title, String description, WorkStatus status) {
//        if (title != null) this.title = title;
//        if (description != null) this.description = description;
//        if (status != null) this.status = status;
//    }
//
//}