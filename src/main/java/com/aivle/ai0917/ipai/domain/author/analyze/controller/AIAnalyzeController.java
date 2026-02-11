package com.aivle.ai0917.ipai.domain.author.analyze.controller;

import com.aivle.ai0917.ipai.domain.author.analyze.dto.AnalyzeRelationshipRequestDto;
import com.aivle.ai0917.ipai.domain.author.analyze.dto.AnalyzeTimelineRequestDto;
import com.aivle.ai0917.ipai.domain.author.analyze.dto.EpisodeBriefDto;
import com.aivle.ai0917.ipai.domain.author.analyze.service.AiAnalyzeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai/author/works/{workId}/analysis")
public class AIAnalyzeController {

    private final AiAnalyzeService aiAnalyzeService;

    /**
     * 1. 인물 관계 분석
     * target이 빈 값("")으로 오면 Service에서 "*"로 변환하여 처리함
     */
    @PostMapping("/relationships")
    public ResponseEntity<?> analyzeRelationships(
            @PathVariable("workId") Long workId,
            @RequestBody AnalyzeRelationshipRequestDto requestDto) {

        log.info("Controller: 인물 관계 분석 요청 workId={}, userId={}", workId, requestDto.getUserId());

        Object result = aiAnalyzeService.analyzeRelationship(
                workId,
                requestDto.getUserId(),
                requestDto.getTarget()
        );

        return ResponseEntity.ok(result);
    }

    /**
     * [추가] 2. 타임라인 분석용 원문(에피소드) 목록 조회
     * GET /api/v1/ai/author/works/{workId}/analysis/timeline/episodes
     * 조건: workId 일치, is_read_only = true
     * 반환: id, ep_num 리스트
     */
    @GetMapping("/timeline/episodes")
    public ResponseEntity<List<EpisodeBriefDto>> getTimelineEpisodes(
            @PathVariable("workId") Long workId) {

        log.info("Controller: 타임라인 원문 목록 조회 요청 workId={}", workId);

        List<EpisodeBriefDto> result = aiAnalyzeService.getTimelineEpisodes(workId);

        return ResponseEntity.ok(result);
    }

    /**
     * 3. 타임라인 분석 요청
     */
    @PostMapping("/timeline")
    public ResponseEntity<?> analyzeTimeline(
            @PathVariable("workId") Long workId,
            @RequestBody AnalyzeTimelineRequestDto requestDto) {

        log.info("Controller: 타임라인 분석 요청 workId={}, userId={}", workId, requestDto.getUserId());

        Object result = aiAnalyzeService.analyzeTimeline(
                workId,
                requestDto.getUserId(),
                requestDto.getTarget()
        );

        return ResponseEntity.ok(result);
    }
}