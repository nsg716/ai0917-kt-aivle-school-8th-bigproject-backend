package com.aivle.ai0917.ipai.domain.author.episodes.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NovelSaveResponseDto {
    private String path; // AI 서버에서 저장한 파일 경로
    private String message; // 성공/실패 메시지
    private boolean success; // 성공 여부
}