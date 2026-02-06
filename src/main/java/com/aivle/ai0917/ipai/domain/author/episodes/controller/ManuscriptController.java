package com.aivle.ai0917.ipai.domain.author.episodes.controller;

import com.aivle.ai0917.ipai.domain.author.episodes.client.AiAnalysisClient;
import com.aivle.ai0917.ipai.domain.author.episodes.dto.CategoryAnalysisRequestDto;
import com.aivle.ai0917.ipai.domain.author.episodes.dto.ManuscriptRequestDto;
import com.aivle.ai0917.ipai.domain.author.episodes.dto.ManuscriptResponseDto;
import com.aivle.ai0917.ipai.domain.author.episodes.dto.ManuscriptUpdateRequestDto;
import com.aivle.ai0917.ipai.domain.author.episodes.service.ManuscriptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/author/{userId}/{title}/manuscript")
public class ManuscriptController {

    private final ManuscriptService manuscriptService;

    // 1. 원문 목록 조회
    @GetMapping("/list")
    public ResponseEntity<Page<ManuscriptResponseDto>> getManuscriptList(
            @PathVariable String userId,
            @PathVariable String title,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<ManuscriptResponseDto> result = manuscriptService.getManuscriptList(userId, title, keyword, pageable);
        return ResponseEntity.ok(result);
    }

    // 2. 원문 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ManuscriptResponseDto> getManuscriptDetail(
            @PathVariable String userId,
            @PathVariable String title,
            @PathVariable Long id) {

        ManuscriptResponseDto result = manuscriptService.getManuscriptDetail(id);
        return ResponseEntity.ok(result);
    }

//    // 3. 원문 등록 (JSON Body)
//    @PostMapping("/upload")
//    public ResponseEntity<Long> uploadManuscript(
//            @PathVariable String userId,
//            @PathVariable String title,
//            @RequestBody ManuscriptRequestDto request) {
//
//        request.setUserId(userId);
//        request.setTitle(title);
//
//        Long savedId = manuscriptService.uploadManuscript(request);
//        return ResponseEntity.ok(savedId);
//    }
    // 3-1. 원문 신규 등록 (Insert)
    // - 회차 자동 생성 기능 포함
    // - 분석 중(is_read_only) 체크 포함
    @PostMapping("/upload")
    public ResponseEntity<Long> createManuscript(
            @PathVariable String userId,
            @PathVariable String title,
            @RequestBody ManuscriptRequestDto request) {

        request.setUserId(userId);
        request.setTitle(title);

        Long savedId = manuscriptService.createManuscript(request);
        return ResponseEntity.ok(savedId);
    }

    // 3-2. 원문 내용 수정 (Update)
    // - 텍스트 파일 덮어쓰기
    // - 분석 중 여부 상관없이 수정 가능 (기존 정책 유지)
    @PatchMapping("/upload")
    public ResponseEntity<Long> modifyManuscript(
            @PathVariable String userId,
            @PathVariable String title,
            @RequestBody ManuscriptRequestDto request) {

        request.setUserId(userId);
        request.setTitle(title);

        Long updatedId = manuscriptService.modifyManuscriptText(request);
        return ResponseEntity.ok(updatedId);
    }

    // 4. 원문 키워드 추출 (수정: JSON Body 사용)
    @PostMapping("/categories")
    public ResponseEntity<AiAnalysisClient.CategoryExtractionResponse> extractCategories(
            @PathVariable String userId,
            @PathVariable String title,
            @RequestBody CategoryAnalysisRequestDto requestDto) { // @RequestBody로 변경

        log.info("카테고리 추출 요청: episodeId={}, workId={}", requestDto.getEpisodeId(), requestDto.getWorkId());

        AiAnalysisClient.CategoryExtractionResponse result = manuscriptService.extractCategories(
                userId, requestDto
        );
        return ResponseEntity.ok(result);
    }

    // 5. 원문 키워드 분석 - 충돌 검토
    @PostMapping("/setting")
    public ResponseEntity<AiAnalysisClient.SettingConflictResponse> checkSetting(
            @PathVariable String userId,
            @PathVariable String title,
            @RequestParam Long workId,
            @RequestBody AiAnalysisClient.CategoryExtractionResponse categories) {

        AiAnalysisClient.SettingConflictResponse result = manuscriptService.checkSettingConflict(
                workId, userId, categories
        );
        return ResponseEntity.ok(result);
    }

    // 6. 원문 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteManuscript(
            @PathVariable String userId,
            @PathVariable String title,
            @PathVariable Long id) {

        manuscriptService.deleteManuscript(id);
        return ResponseEntity.ok("삭제 완료");
    }

    // 7. 원문 정보(소제목, 회차) 변경
    // PATCH /api/v1/author/{userId}/{title}/manuscript/{id}
    @PatchMapping("/{id}")
    public ResponseEntity<String> updateManuscript(
            @PathVariable String userId,
            @PathVariable String title,
            @PathVariable Long id,
            @RequestBody ManuscriptUpdateRequestDto request) {

        manuscriptService.updateManuscript(id, request);
        return ResponseEntity.ok("수정 완료");
    }
}