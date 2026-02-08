package com.aivle.ai0917.ipai.domain.manager.ipext.controller;

import com.aivle.ai0917.ipai.domain.manager.ipext.dto.IpProposalRequestDto;
import com.aivle.ai0917.ipai.domain.manager.ipext.dto.IpProposalResponseDto;
import com.aivle.ai0917.ipai.domain.manager.ipext.service.IpextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/manager/ipext") // [일반 기능 Prefix]
@RequiredArgsConstructor
public class IpextController {

    private final IpextService ipextService;

    // 1. IP 확장 조회 (목록) - ManagerId 별 조회
    // GET /api/v1/manager/ipext/{managerId}
    @GetMapping("/{managerId}")
    public ResponseEntity<Page<IpProposalResponseDto>> getProposalList(
            @PathVariable String managerId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        // PageableDefault를 통해 sort, page 파라미터를 자동으로 처리합니다.
        // 예: ?page=0&size=10&sort=createdAt,desc
        return ResponseEntity.ok(ipextService.getProposalList(managerId, pageable));
    }

    // 2. IP 확장 제안 상세 조회
    // GET /api/v1/manager/ipext/{managerId}/{id}
    @GetMapping("/{managerId}/{id}")
    public ResponseEntity<IpProposalResponseDto> getProposalDetail(
            @PathVariable String managerId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ipextService.getProposalDetail(managerId, id));
    }

    // 3. IP 확장 제안 수정
    // PATCH /api/v1/manager/ipext/{managerId}/{id}
    @PatchMapping("/{managerId}/{id}")
    public ResponseEntity<String> updateProposal(
            @PathVariable String managerId,
            @PathVariable Long id,
            @RequestBody IpProposalRequestDto request) {
        ipextService.updateProposal(managerId, id, request);
        return ResponseEntity.ok("수정 완료");
    }

    // 4. IP 확장 제안 삭제
    // DELETE /api/v1/manager/ipext/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProposal(@PathVariable Long id) {
        ipextService.deleteProposal(id);
        return ResponseEntity.ok("삭제 완료");
    }

    // 5. IP 확장 제안 기획서 미리보기
    // GET /api/v1/manager/ipext/preview/{id}
    @GetMapping("/preview/{id}")
    public ResponseEntity<IpProposalResponseDto> getProposalPreview(@PathVariable Long id) {
        return ResponseEntity.ok(ipextService.getProposalPreview(id));
    }

    // 6. 1단계 매칭된 작가 표시
    // GET /api/v1/manager/ipext/{managerId}/author/
    @GetMapping("/{managerId}/author")
    public ResponseEntity<Object> getMatchedAuthors(@PathVariable String managerId) {
        return ResponseEntity.ok(ipextService.getMatchedAuthors(managerId));
    }

    // 7. 1단계 매칭 작가 작품 표시
    // GET /api/v1/manager/ipext/{managerId}/authorwork
    @GetMapping("/{managerId}/authorwork")
    public ResponseEntity<Object> getAuthorWorks(@PathVariable String managerId) {
        return ResponseEntity.ok(ipextService.getAuthorWorks(managerId));
    }

    // 8. 1단계 작품 설정집 표시
    // GET /api/v1/manager/ipext/{workId}/authorworklorebook
    @GetMapping("/{workId}/authorworklorebook")
    public ResponseEntity<Object> getWorkLorebooks(@PathVariable Long workId) {
        return ResponseEntity.ok(ipextService.getWorkLorebooks(workId));
    }
}