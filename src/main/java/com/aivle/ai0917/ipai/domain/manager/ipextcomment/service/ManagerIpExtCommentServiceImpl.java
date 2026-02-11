package com.aivle.ai0917.ipai.domain.manager.ipextcomment.service;

import com.aivle.ai0917.ipai.domain.author.ipextcomment.model.IpProposalComment;
import com.aivle.ai0917.ipai.domain.author.ipextcomment.repository.IpProposalCommentRepository;
import com.aivle.ai0917.ipai.domain.manager.ipextcomment.dto.ManagerCommentResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerIpExtCommentServiceImpl implements ManagerIpExtCommentService {

    private final IpProposalCommentRepository ipProposalCommentRepository;

    @Override
    public List<ManagerCommentResponseDto> getCommentsByProposal(Long proposalId) {
        // 1. 제안서 ID에 달린 모든 코멘트 조회
        List<IpProposalComment> comments = ipProposalCommentRepository.findAllByIpProposalId(proposalId);

        // 2. DTO 변환 (미사용 'ARCHIVED' 상태 필터링 추가)
        return comments.stream()
                // [추가] 상태가 ARCHIVED(미사용)가 아닌 것만 필터링
                .filter(comment -> comment.getStatus() != IpProposalComment.Status.ARCHIVED)
                .map(ManagerCommentResponseDto::new)
                .collect(Collectors.toList());
    }
}