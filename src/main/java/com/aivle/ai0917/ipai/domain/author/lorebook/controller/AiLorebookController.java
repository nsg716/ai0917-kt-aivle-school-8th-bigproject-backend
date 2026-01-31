package com.aivle.ai0917.ipai.domain.author.lorebook.controller;

import com.aivle.ai0917.ipai.domain.author.lorebook.client.AiLorebookClient;
import com.aivle.ai0917.ipai.domain.author.lorebook.dto.SettingBookCreateRequestDto;
import com.aivle.ai0917.ipai.domain.author.lorebook.dto.SettingBookUpdateRequestDto;
import com.aivle.ai0917.ipai.domain.author.lorebook.service.SettingBookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/ai/author")
@RequiredArgsConstructor
public class AiLorebookController {

    private final SettingBookService lorebookService;

    // 3. 설정집 수동 저장 및 AI 비교 (API 연쇄 동작)
    // POST /api/v1/ai/author/{userId}/{title}/lorebook/setting_save
    @PostMapping("/{userId}/{title}/lorebook/setting_save")
    public ResponseEntity<AiLorebookClient.ManualComparisonResponse> createSetting(
            @PathVariable String userId,
            @PathVariable String title,
            @RequestParam Long workId,
            @RequestBody SettingBookCreateRequestDto request
    ) {
        // DB 저장 후 AI 분석 결과를 바로 리턴
        AiLorebookClient.ManualComparisonResponse response = lorebookService.create(userId, workId, request);
        return ResponseEntity.ok(response);
    }

    // 5. 설정집 수정
    @PatchMapping("/{userId}/{title}/lorebook/{tags}/{id}")
    public ResponseEntity<String> update(
            @PathVariable String userId,
            @PathVariable String title,
            @PathVariable String tags,
            @PathVariable Long id,
            @RequestBody SettingBookUpdateRequestDto request
    ) {
        lorebookService.update(id, userId, request);
        return ResponseEntity.ok("수정 완료");
    }

    // 6. 설정집 유사도 검색
    @PostMapping("/{userId}/{title}/lorebook/userq")
    public ResponseEntity<List<Object>> searchSimilarLore(
            @PathVariable String userId,
            @PathVariable String title,
            @RequestBody AiLorebookClient.LorebookSearchRequest request
    ) {
        List<Object> result = lorebookService.searchSimilarLore(
                request.getUserId(),
                request.getWorkId(),
                request.getUserQuery(),
                request.getCategory()
        );
        return ResponseEntity.ok(result);
    }
}