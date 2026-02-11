package com.aivle.ai0917.ipai.domain.author.analyze.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AnalyzeRelationshipRequestDto {

    @JsonProperty("user_id")
    private String userId;

    // '*' 또는 'keyword'
    @JsonProperty("target")
    private String target;
}