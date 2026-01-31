package com.aivle.ai0917.ipai.domain.author.episodes.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryAnalysisRequestDto {
    private Long episodeId;
    private Long workId;
    private Integer epNum;
    private String subtitle;
}