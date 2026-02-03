package com.aivle.ai0917.ipai.domain.author.episodes.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ManuscriptUpdateRequestDto {
    private String subtitle; // 변경할 소제목
    private Integer epNum;
}