package com.aivle.ai0917.ipai.domain.manager.ipext.dto;

import com.aivle.ai0917.ipai.domain.manager.ipext.model.IpProposal;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class IpProposalResponseDto {
    private Long id;
    private String managerId;
    private String title;
    private IpProposal.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 상세 정보
    private List<Long> lorebookIds;
    private IpProposal.TargetFormat targetFormat;
    private String targetGenre;
    private IpProposal.WorldSetting worldSetting;
    private List<String> targetAges;
    private IpProposal.TargetGender targetGender;
    private IpProposal.BudgetScale budgetScale;
    private String toneAndManner;
    private JsonNode mediaDetail;
    private String addPrompt;

    // 요약
    private String expMarket;
    private String expCreative;
    private String expVisual;
    private String expWorld;
    private String expBusiness;
    private String expProduction;

    // 파일 정보
    private String filePath;

    public IpProposalResponseDto(IpProposal entity) {
        this.id = entity.getId();
        this.managerId = entity.getManagerId();
        this.title = entity.getTitle();
        this.status = entity.getStatus();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();

        this.lorebookIds = entity.getLorebookIds();
        this.targetFormat = entity.getTargetFormat();
        this.targetGenre = entity.getTargetGenre();
        this.worldSetting = entity.getWorldSetting();
        this.targetAges = entity.getTargetAges();
        this.targetGender = entity.getTargetGender();
        this.budgetScale = entity.getBudgetScale();
        this.toneAndManner = entity.getToneAndManner();
        this.mediaDetail = entity.getMediaDetail();
        this.addPrompt = entity.getAddPrompt();

        this.expMarket = entity.getExpMarket();
        this.expCreative = entity.getExpCreative();
        this.expVisual = entity.getExpVisual();
        this.expWorld = entity.getExpWorld();
        this.expBusiness = entity.getExpBusiness();
        this.expProduction = entity.getExpProduction();

        this.filePath = entity.getFilePath();
    }
}