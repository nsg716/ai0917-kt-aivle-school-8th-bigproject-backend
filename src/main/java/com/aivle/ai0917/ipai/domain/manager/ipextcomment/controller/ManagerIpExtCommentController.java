package com.aivle.ai0917.ipai.domain.manager.ipextcomment.controller;

import com.aivle.ai0917.ipai.domain.manager.ipextcomment.dto.ManagerCommentResponseDto;
import com.aivle.ai0917.ipai.domain.manager.ipextcomment.service.ManagerIpExtCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/manager/ipext/comment") // [매니저용 코멘트 API Prefix]
@RequiredArgsConstructor
public class ManagerIpExtCommentController {

    private final ManagerIpExtCommentService managerIpExtCommentService;

    // 매니저 - 특정 제안서의 모든 코멘트 조회
    // GET /api/v1/manager/ipext/comment/{id}
    @GetMapping("/{id}")
    public ResponseEntity<List<ManagerCommentResponseDto>> getProposalComments(
            @PathVariable("id") Long proposalId) {

        log.info("매니저 IP 확장 코멘트 조회 요청: proposalId={}", proposalId);

        List<ManagerCommentResponseDto> comments = managerIpExtCommentService.getCommentsByProposal(proposalId);

        return ResponseEntity.ok(comments);
    }
}