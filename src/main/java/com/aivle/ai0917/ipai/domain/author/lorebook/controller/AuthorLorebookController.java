package com.aivle.ai0917.ipai.domain.author.lorebook.controller;

import com.aivle.ai0917.ipai.domain.author.lorebook.dto.SettingBookResponseDto;
import com.aivle.ai0917.ipai.domain.author.lorebook.service.SettingBookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/author") // [일반 기능 Prefix]
@RequiredArgsConstructor
public class AuthorLorebookController {

    private final SettingBookService lorebookService;

    // 1. 설정집 조회 (전체)
    // GET /api/v1/author/{userId}/{title}/lorebook
    @GetMapping("/{userId}/{title}/lorebook")
    public ResponseEntity<Page<SettingBookResponseDto>> getLorebookMain(
            @PathVariable String userId,
            @PathVariable String title,
            @RequestParam Long workId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(lorebookService.getLorebookList(userId, workId, pageable));
    }

    // 2. 설정집 카테고리 조회
    // GET /api/v1/author/{userId}/{title}/lorebook/{category}
    @GetMapping("/{userId}/{title}/lorebook/{category}")
    public ResponseEntity<List<SettingBookResponseDto>> getByCategory(
            @PathVariable String userId,
            @PathVariable String title,
            @PathVariable String category,
            @RequestParam Long workId
    ) {
        return ResponseEntity.ok(
                lorebookService.getItemsByCategory(userId, workId, category)
        );
    }

    // 4. 설정집 삭제
    // DELETE /api/v1/author/{userId}/{title}/lorebook/{tags}/{id}
    @DeleteMapping("/{userId}/{title}/lorebook/{tags}/{id}")
    public ResponseEntity<String> delete(
            @PathVariable String userId,
            @PathVariable String title,
            @PathVariable String tags,
            @PathVariable Long id
    ) {
        lorebookService.delete(id);
        return ResponseEntity.ok("삭제 완료");
    }

    // 7. 설정집 다운로드
    // GET /api/v1/author/{userId}/{title}/lorebook/download/{id}
    @GetMapping("/{userId}/{title}/lorebook/download/{id}")
    public ResponseEntity<String> downloadLorebook(
            @PathVariable String userId,
            @PathVariable String title,
            @PathVariable Long id
    ) {
        // 실제 다운로드 로직 구현 필요
        return ResponseEntity.ok("다운로드 기능은 준비 중입니다.");
    }
}