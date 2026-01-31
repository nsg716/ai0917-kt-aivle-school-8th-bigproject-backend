package com.aivle.ai0917.ipai.domain.author.episodes.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManuscriptRequestDto {
    private String userId;
    private Long workId; // AI 연동 및 DB 저장을 위해 필수
    private String title; // 작품 제목
    private Integer episode; // 회차 번호 (ep_num)
    private String subtitle; // 소제목
    private String txt;
}