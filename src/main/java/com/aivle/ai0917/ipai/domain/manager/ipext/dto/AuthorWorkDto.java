package com.aivle.ai0917.ipai.domain.manager.ipext.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AuthorWorkDto {
    private Long workId;
    private String title;
    private String primaryAuthorId; // 작가 ID
    private String authorName;      // 작가 이름 (선택사항, 필요시 쿼리에서 가져옴)
    private String genre;
    private String status;
    private String coverImageUrl;
    private LocalDateTime createdAt;
}