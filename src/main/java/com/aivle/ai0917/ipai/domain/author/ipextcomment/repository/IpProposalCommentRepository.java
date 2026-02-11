package com.aivle.ai0917.ipai.domain.author.ipextcomment.repository;

import com.aivle.ai0917.ipai.domain.author.ipextcomment.model.IpProposalComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IpProposalCommentRepository extends JpaRepository<IpProposalComment, Long> {

    // 특정 제안서(ipProposalId)에 대해 특정 작가(userId)가 남긴 코멘트 조회
    Optional<IpProposalComment> findByIpProposalIdAndAuthorIdAndStatusNot(Long ipProposalId, String authorId, IpProposalComment.Status status);

    // [추가] 특정 제안서에 달린 "모든" 코멘트 조회 (매니저가 일괄 수정할 때 사용)
    List<IpProposalComment> findAllByIpProposalId(Long ipProposalId);
}