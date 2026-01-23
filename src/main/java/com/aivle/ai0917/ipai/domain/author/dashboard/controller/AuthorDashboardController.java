package com.aivle.ai0917.ipai.domain.author.dashboard.controller;

import com.aivle.ai0917.ipai.domain.author.dashboard.dto.DashboardSummaryResponseDto;
import com.aivle.ai0917.ipai.domain.author.dashboard.service.DashboardService;
import com.aivle.ai0917.ipai.domain.notice.dto.NoticeResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/author/dashboard")
@RequiredArgsConstructor
public class AuthorDashboardController {

    private final DashboardService dashboardService;

    /**
     * 작가 활동 요약 정보 조회 (상단 카드용)
     * 호출 예: /api/v1/author/dashboard/summary?authorId=9
     */
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponseDto> getSummary(
            @RequestParam("authorId") Long authorId) { // String에서 Long으로 변경
        return ResponseEntity.ok(dashboardService.getDashboardSummary(authorId));
    }

    /**
     * 특정 ID(예: 9번 작가)를 고정으로 호출하는 테스트용 API
     */
    @GetMapping("/summary/test")
    public ResponseEntity<DashboardSummaryResponseDto> getTestSummary() {
        // 더 이상 "{미상}" 문자열을 보낼 수 없으므로, DB에 존재하는 작가 ID(Long)를 전달합니다.
        Long testAuthorId = 9L;
        return ResponseEntity.ok(dashboardService.getDashboardSummary(testAuthorId));
    }

    /**
     * 대시보드용 최신 공지 조회 (하단 목록용)
     */
    @GetMapping("/notice")
    public ResponseEntity<Page<NoticeResponseDto>> getDashboardNotices(
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(dashboardService.getDashboardNotices(pageable));
    }
}