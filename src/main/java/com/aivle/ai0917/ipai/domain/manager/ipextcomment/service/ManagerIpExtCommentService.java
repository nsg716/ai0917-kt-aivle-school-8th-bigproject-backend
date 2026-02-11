package com.aivle.ai0917.ipai.domain.manager.ipextcomment.service;

// [수정] 경로 수정 (ipext -> ipextcomment)
import com.aivle.ai0917.ipai.domain.manager.ipextcomment.dto.ManagerCommentResponseDto;
import java.util.List;

public interface ManagerIpExtCommentService {
    // 제안서 ID로 모든 코멘트 조회
    List<ManagerCommentResponseDto> getCommentsByProposal(Long proposalId);
}