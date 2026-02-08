package com.aivle.ai0917.ipai.domain.manager.ipext.dto;

import com.aivle.ai0917.ipai.domain.manager.ipext.model.IpProposal;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class IpProposalRequestDto {
    // 기본 정보
    private String managerId;
    private String title;

    // 1단계: 선택한 설정집 ID 목록
    private List<Long> lorebookIds;

    @JsonProperty("processed_lorebooks")
    private List<Map<String, Object>> processedLorebooks;


    // 3단계: 확장/장르 (전략)
    private IpProposal.TargetFormat targetFormat;
    private String targetGenre; // 텍스트 입력
    private IpProposal.WorldSetting worldSetting;

    // 4단계: 비즈니스 전략
    private List<String> targetAges;
    private IpProposal.TargetGender targetGender;
    private IpProposal.BudgetScale budgetScale;
    private String toneAndManner;

    // 5단계: 매체 상세 설정 (JSON 그대로 받음)
    private JsonNode mediaDetail;

    // 추가 프롬프트
    private String addPrompt;

    // 요약 정보 (생성 시 AI가 만들어줄 수도 있지만, 수정 시 필요)
    private String summary1;
    private String summary2;
    private String summary3;
    private String summary4;
    private String summary5;
    private String summary6;
}