package com.aivle.ai0917.ipai.domain.manager.ipext.controller;

import com.aivle.ai0917.ipai.domain.manager.ipext.client.AiIpExtClient;
import com.aivle.ai0917.ipai.domain.manager.ipext.dto.ConflictCheckRequestDto;
import com.aivle.ai0917.ipai.domain.manager.ipext.dto.IpProposalRequestDto;
import com.aivle.ai0917.ipai.domain.manager.ipext.service.IpextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/ai/manager/ipext") // [AI 기능 Prefix]
@RequiredArgsConstructor
public class AiIpextController {

    private final IpextService ipextService;

    // 9. 2단계 설정집 충돌 여부 (AI)
    // POST /api/v1/ai/manager/ipext/settings
    @PostMapping("/settings")
    public ResponseEntity<AiIpExtClient.LorebookCheckResponse> checkSettingsConflict(@RequestBody ConflictCheckRequestDto request) {
        log.info("AI 설정 충돌 검사 요청");
        return ResponseEntity.ok(ipextService.checkSettingsConflict(request));
    }
    // 10. IP 확장 제안 등록 (DB 저장)
    // POST /api/v1/ai/manager/ipext
    // (등록 과정에 AI 분석이나 생성이 포함될 수 있으므로 AI 컨트롤러에 배치)
    @PostMapping
    public ResponseEntity<AiIpExtClient.ProposalResponse> createProposal(@RequestBody IpProposalRequestDto request) {
        log.info("IP 확장 제안 등록 및 PDF 생성 요청: {}", request.getTitle());
        AiIpExtClient.ProposalResponse response = ipextService.createProposal(request);
        return ResponseEntity.ok(response);
    }
}