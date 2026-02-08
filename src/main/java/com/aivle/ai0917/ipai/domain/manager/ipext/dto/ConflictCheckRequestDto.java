package com.aivle.ai0917.ipai.domain.manager.ipext.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

// 충돌 검사 요청 DTO
@Getter
@Builder
public class ConflictCheckRequestDto {
    @JsonProperty("lorebooks")

    private List<Map<String, Object>> lorebooks;
}