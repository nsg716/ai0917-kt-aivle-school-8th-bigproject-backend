package com.aivle.ai0917.ipai.domain.manager.ipext.service;

import com.aivle.ai0917.ipai.domain.manager.ipext.dto.IpProposalRequestDto;
import com.aivle.ai0917.ipai.domain.manager.ipext.model.IpProposal;
import com.aivle.ai0917.ipai.domain.manager.ipext.repository.IpProposalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IpProposalSaveService {

    private final IpProposalRepository ipProposalRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW) // ⭐ 새로운 트랜잭션 시작
    public Long saveProposal(IpProposalRequestDto request) {
        IpProposal proposal = IpProposal.builder()
                .managerId(request.getManagerId())
                .title(request.getTitle())
                .lorebookIds(request.getLorebookIds())
                .targetFormat(request.getTargetFormat())
                .targetGenre(request.getTargetGenre())
                .worldSetting(request.getWorldSetting())
                .targetAges(request.getTargetAges())
                .targetGender(request.getTargetGender())
                .budgetScale(request.getBudgetScale())
                .toneAndManner(request.getToneAndManner())
                .mediaDetail(request.getMediaDetail())
                .addPrompt(request.getAddPrompt())
                .expMarket(request.getSummary1())
                .expCreative(request.getSummary2())
                .expVisual(request.getSummary3())
                .expWorld(request.getSummary4())
                .expBusiness(request.getSummary5())
                .expProduction(request.getSummary6())
                .status(IpProposal.Status.NEW)
                .build();

        IpProposal saved = ipProposalRepository.save(proposal);
        log.info("제안서 DB 저장 완료: ID={}", saved.getId());

        return saved.getId();
    } // ⭐ 메서드 종료 시 트랜잭션 커밋 → DB에 즉시 반영됨
}