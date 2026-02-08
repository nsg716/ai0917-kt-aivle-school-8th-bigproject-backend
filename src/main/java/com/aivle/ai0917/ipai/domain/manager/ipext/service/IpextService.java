package com.aivle.ai0917.ipai.domain.manager.ipext.service;

import com.aivle.ai0917.ipai.domain.manager.ipext.client.AiIpExtClient;
import com.aivle.ai0917.ipai.domain.manager.ipext.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IpextService {

    // [Manager] 목록 조회 (managerId 추가)
    Page<IpProposalResponseDto> getProposalList(String managerId, Pageable pageable);

    // [Manager] 상세 조회 (managerId 추가 - 권한 확인용)
    IpProposalResponseDto getProposalDetail(String managerId, Long id);

    // [Manager] 수정 (managerId 추가 - 권한 확인용)
    void updateProposal(String managerId, Long id, IpProposalRequestDto request);

    // [Manager] 삭제
    void deleteProposal(Long id);

    // [Manager] 기획서 미리보기
    IpProposalResponseDto getProposalPreview(Long id);

    // [1단계] 매칭된 작가 조회
    List<AuthorMatchResponseDto> getMatchedAuthors(String managerId);

    // [1단계] 작가 작품 조회
    List<AuthorWorkDto> getAuthorWorks(String managerId);

    // [1단계] 작품 설정집 조회
    List<MatchedLorebookDto> getWorkLorebooks(Long workId);

    // [2단계] 설정집 충돌 여부 확인 (AI)
    // 리턴 타입 변경: Object -> AiIpExtClient.LorebookCheckResponse
    AiIpExtClient.LorebookCheckResponse checkSettingsConflict(ConflictCheckRequestDto request);

    // [등록] IP 확장 제안 등록 (DB 저장 + AI 요청)
    // 리턴 타입 변경: Long -> AiIpExtClient.ProposalResponse
    AiIpExtClient.ProposalResponse createProposal(IpProposalRequestDto request);



}