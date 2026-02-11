package com.aivle.ai0917.ipai.domain.author.ipextcomment.controller;

import com.aivle.ai0917.ipai.domain.author.ipextcomment.dto.AuthorCommentRequestDto;
import com.aivle.ai0917.ipai.domain.author.ipextcomment.dto.AuthorProposalDetailDto;
import com.aivle.ai0917.ipai.domain.author.ipextcomment.dto.AuthorProposalListDto;
import com.aivle.ai0917.ipai.domain.author.ipextcomment.service.AuthorIpExtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/author/ipext/comment")
@RequiredArgsConstructor
public class AuthorIpExtCommentController {

    private final AuthorIpExtService authorIpExtService;

    // 1. 작가 자신에게 매칭된 IP 확장 목록 조회
    @GetMapping
    public ResponseEntity<Page<AuthorProposalListDto>> getMyProposals(
            @RequestParam String authorId,
            @PageableDefault(size = 10, sort = "created_at", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("작가 제안서 목록 조회 요청: authorId={}", authorId);
        return ResponseEntity.ok(authorIpExtService.getMatchedProposals(authorId, pageable));
    }

    // 2. IP 확장 목록 상세보기
    @GetMapping("/{id}")
    public ResponseEntity<AuthorProposalDetailDto> getProposalDetail(
            @PathVariable Long id,
            @RequestParam String authorId) {

        log.info("작가 제안서 상세 조회 요청: id={}, authorId={}", id, authorId);
        return ResponseEntity.ok(authorIpExtService.getProposalDetail(authorId, id));
    }

    // 3. 코멘트 등록 (POST)
    @PostMapping
    public ResponseEntity<String> addComment(@RequestBody AuthorCommentRequestDto request) {
        log.info("작가 코멘트 등록 요청: proposalId={}, status={}", request.getProposalId(), request.getStatus());
        authorIpExtService.addComment(request);
        return ResponseEntity.ok("코멘트가 등록되었습니다.");
    }

    // 4. [추가] 코멘트 수정 (PATCH)
    // URL의 {id}는 Proposal ID입니다. (작가는 1기획서 1코멘트이므로)
    @PatchMapping("/{id}")
    public ResponseEntity<String> updateComment(
            @PathVariable Long id,
            @RequestBody AuthorCommentRequestDto request) {

        log.info("작가 코멘트 수정 요청: proposalId={}, authorId={}", id, request.getAuthorIntegrationId());

        // PathVariable의 Proposal ID가 RequestBody보다 우선하도록 처리하거나 검증 가능
        // Service로 넘길 때 ID를 명확히 전달
        authorIpExtService.updateComment(id, request);
        return ResponseEntity.ok("코멘트가 수정되었습니다.");
    }
}