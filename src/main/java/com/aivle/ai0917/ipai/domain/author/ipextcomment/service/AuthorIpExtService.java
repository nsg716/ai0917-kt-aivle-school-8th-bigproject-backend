package com.aivle.ai0917.ipai.domain.author.ipextcomment.service;

import com.aivle.ai0917.ipai.domain.author.ipextcomment.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuthorIpExtService {
    // 매칭된 제안서 목록 조회
    Page<AuthorProposalListDto> getMatchedProposals(String authorId, Pageable pageable);

    // 제안서 상세 조회
    AuthorProposalDetailDto getProposalDetail(String authorId, Long proposalId);

    // 코멘트 등록 및 상태 변경
    void addComment(AuthorCommentRequestDto request);

    void updateComment(Long proposalId, AuthorCommentRequestDto request);
}