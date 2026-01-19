package com.aivle.ai0917.ipai.domain.notice.dto;

import com.aivle.ai0917.ipai.domain.notice.model.Notice;
import lombok.Getter;

import java.time.Instant;

@Getter
public class NoticeResponseDto {
    private Long id;
    private String title;
    private String content;
    private String filePath;
    private String originalFilename;  // 원본 파일명 추가
    private Long fileSize;            // 파일 크기 추가
    private String contentType;       // MIME 타입 추가
    private String writer;
    private Instant createdAt;

    public NoticeResponseDto(Notice notice) {
        this.id = notice.getId();
        this.title = notice.getTitle();
        this.content = notice.getContent();
        this.filePath = notice.getFilePath();
        this.originalFilename = notice.getOriginalFilename();
        this.fileSize = notice.getFileSize();
        this.contentType = notice.getContentType();
        this.writer = notice.getWriter();
        this.createdAt = notice.getCreatedAt();
    }
}