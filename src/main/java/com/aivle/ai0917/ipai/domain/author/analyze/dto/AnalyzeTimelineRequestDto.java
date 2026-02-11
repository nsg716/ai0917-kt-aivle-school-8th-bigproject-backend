package com.aivle.ai0917.ipai.domain.author.analyze.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AnalyzeTimelineRequestDto {

    @JsonProperty("user_id")
    private String userId;

    // 회차 리스트 [1, 2, 3...]
    @JsonProperty("target")
    private List<Integer> target;
}