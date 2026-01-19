package com.aivle.ai0917.ipai.domain.notice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notices")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type")
    private String contentType;

    @Column(nullable = false, length = 50)
    private String writer;

    @Column(updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @Builder
    public Notice(String title, String content, String filePath, String originalFilename,
                  Long fileSize, String contentType, String writer) {
        this.title = title;
        this.content = content;
        this.filePath = filePath;
        this.originalFilename = originalFilename;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.writer = writer;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // 파일 관련 비즈니스 로직
    public boolean hasFile() {
        return this.filePath != null && !this.filePath.isEmpty();
    }

    public void updateFile(String filePath, String originalFilename, Long fileSize, String contentType) {
        this.filePath = filePath;
        this.originalFilename = originalFilename;
        this.fileSize = fileSize;
        this.contentType = contentType;
    }

    public void removeFile() {
        this.filePath = null;
        this.originalFilename = null;
        this.fileSize = null;
        this.contentType = null;
    }
}