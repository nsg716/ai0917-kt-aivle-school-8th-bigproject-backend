package com.aivle.ai0917.ipai.domain.author.lorebook.service;

import com.aivle.ai0917.ipai.domain.author.lorebook.client.AiLorebookClient;
import com.aivle.ai0917.ipai.domain.author.lorebook.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SettingBookService {

    // 전체 조회
    Page<SettingBookResponseDto> getLorebookList(String userId, Long workId, Pageable pageable);

    // 카테고리별 조회
    List<SettingBookResponseDto> getItemsByCategory(String userId, Long workId, String category);

    // [수정] 생성 및 AI 비교 (반환 타입 변경: void -> ManualComparisonResponse)
    AiLorebookClient.ManualComparisonResponse create(String userId, Long workId, SettingBookCreateRequestDto request);

    // 수정
    void update(Long id, String userId, SettingBookUpdateRequestDto request);

    // 삭제
    void delete(Long id);

    // AI 유사도 검색
    List<Object> searchSimilarLore(String userId, Long workId, String query, String category);
}