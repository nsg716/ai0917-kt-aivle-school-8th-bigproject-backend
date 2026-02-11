package com.aivle.ai0917.ipai.domain.author.ipextcomment.service;

import com.aivle.ai0917.ipai.domain.author.ipextcomment.dto.*;
import com.aivle.ai0917.ipai.domain.author.ipextcomment.model.IpProposalComment;
import com.aivle.ai0917.ipai.domain.author.ipextcomment.repository.IpProposalCommentRepository;
import com.aivle.ai0917.ipai.domain.manager.ipext.model.IpProposal;
import com.aivle.ai0917.ipai.domain.manager.ipext.repository.IpProposalRepository;
import com.aivle.ai0917.ipai.domain.user.model.User;
import com.aivle.ai0917.ipai.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthorIpExtServiceImpl implements AuthorIpExtService {

    private final IpProposalRepository ipProposalRepository;
    private final IpProposalCommentRepository ipProposalCommentRepository;
    private final UserRepository userRepository;

    @Override
    public Page<AuthorProposalListDto> getMatchedProposals(String authorId, Pageable pageable) {
        return ipProposalRepository.findAllByMatchAuthorIdContains(authorId, pageable)
                .map(AuthorProposalListDto::new);
    }

    @Override
    public AuthorProposalDetailDto getProposalDetail(String authorId, Long proposalId) {
        // 1. 제안서 조회
        IpProposal proposal = ipProposalRepository.findActiveByIdAndAuthorId(proposalId, authorId)
                .orElseThrow(() -> new NoSuchElementException("제안서를 찾을 수 없거나 열람 권한이 없습니다."));

        // 2. [수정] 코멘트 조회 시 '미사용(ARCHIVED)' 상태인 것은 제외하고 최신 유효 코멘트만 가져옴
        AuthorCommentDto myComment = ipProposalCommentRepository.findByIpProposalIdAndAuthorIdAndStatusNot(
                        proposalId,
                        authorId,
                        IpProposalComment.Status.ARCHIVED // 제외할 상태
                )
                .map(AuthorCommentDto::new)
                .orElse(null);

        return new AuthorProposalDetailDto(proposal, myComment);
    }

    @Override
    @Transactional
    public void addComment(AuthorCommentRequestDto request) {
        // 1. [수정] 이미 등록된 '유효한' 코멘트가 있는지 확인
        // ARCHIVED 상태의 코멘트가 있더라도, StatusNot으로 조회하면 결과가 없으므로 통과 -> 재등록 가능
        if (ipProposalCommentRepository.findByIpProposalIdAndAuthorIdAndStatusNot(
                request.getProposalId(),
                request.getAuthorIntegrationId(),
                IpProposalComment.Status.ARCHIVED).isPresent()) {
            throw new IllegalStateException("이미 해당 제안서에 대한 코멘트가 등록되어 있습니다. 수정 기능을 이용해주세요.");
        }

        // 2. 제안서 확인
        IpProposal proposal = ipProposalRepository.findActiveByIdAndAuthorId(request.getProposalId(), request.getAuthorIntegrationId())
                .orElseThrow(() -> new NoSuchElementException("유효하지 않은 제안서입니다."));

        // 3. 작가 이름 조회
        User author = userRepository.findByIntegrationId(request.getAuthorIntegrationId())
                .orElseThrow(() -> new NoSuchElementException("작가 정보를 찾을 수 없습니다."));

        // 4. 상태 변환
        IpProposalComment.Status status;
        try {
            status = IpProposalComment.Status.valueOf(request.getStatus());
        } catch (Exception e) {
            status = IpProposalComment.Status.PENDING;
        }

        // 5. 저장
        IpProposalComment comment = IpProposalComment.builder()
                .ipProposal(proposal)
                .authorId(request.getAuthorIntegrationId())
                .authorName(author.getName())
                .managerId(proposal.getManagerId())
                .status(status)
                .comment(request.getComment())
                .build();

        ipProposalCommentRepository.save(comment);
        log.info("작가({}) 코멘트 등록 완료", author.getName());
    }

    @Override
    @Transactional
    public void updateComment(Long proposalId, AuthorCommentRequestDto request) {
        // 1. [수정] 수정할 때도 '유효한(ARCHIVED가 아닌)' 코멘트를 찾아서 수정
        IpProposalComment comment = ipProposalCommentRepository.findByIpProposalIdAndAuthorIdAndStatusNot(
                        proposalId,
                        request.getAuthorIntegrationId(),
                        IpProposalComment.Status.ARCHIVED
                )
                .orElseThrow(() -> new NoSuchElementException("수정할 코멘트가 존재하지 않습니다. 먼저 등록해주세요."));

        // 2. 상태 변환
        IpProposalComment.Status status = null;
        if (request.getStatus() != null) {
            try {
                status = IpProposalComment.Status.valueOf(request.getStatus());
            } catch (Exception e) {
                // 무시
            }
        }

        // 3. 업데이트
        comment.update(status, request.getComment());
        log.info("작가({}) 코멘트 수정 완료: 제안서 ID={}", request.getAuthorIntegrationId(), proposalId);
    }
}