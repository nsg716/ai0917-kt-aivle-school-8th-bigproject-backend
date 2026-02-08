package com.aivle.ai0917.ipai.domain.manager.ipext.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MatchedLorebookDto {
    private Long lorebookId;
    private String keyword;       // 설정집 키워드 (제목 역할)
    private String category;      // 인물, 장소, 사건 등
    private String description;   // 설정 내용 (JSON String or Summary)
    private Long workId;          // 소속 작품 ID
    private String workTitle;     // 소속 작품 제목 (UI 표시용)
}