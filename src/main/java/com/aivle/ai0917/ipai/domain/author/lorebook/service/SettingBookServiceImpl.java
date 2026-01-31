package com.aivle.ai0917.ipai.domain.author.lorebook.service;

import com.aivle.ai0917.ipai.domain.author.lorebook.client.AiLorebookClient;
import com.aivle.ai0917.ipai.domain.author.lorebook.dto.*;
import com.aivle.ai0917.ipai.domain.author.lorebook.repository.SettingBookCommandRepository;
import com.aivle.ai0917.ipai.domain.author.lorebook.repository.SettingBookViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettingBookServiceImpl implements SettingBookService {

    private final SettingBookViewRepository viewRepository;
    private final SettingBookCommandRepository commandRepository;
    private final AiLorebookClient aiLorebookClient;

    @Override
    public Page<SettingBookResponseDto> getLorebookList(String userId, Long workId, Pageable pageable) {
        return viewRepository.findByUserIdAndWorkId(userId, workId, pageable)
                .map(SettingBookResponseDto::new);
    }

    @Override
    public List<SettingBookResponseDto> getItemsByCategory(String userId, Long workId, String category) {
        return viewRepository.findByUserIdAndWorkIdAndCategory(userId, workId, category)
                .stream()
                .map(SettingBookResponseDto::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AiLorebookClient.ManualComparisonResponse create(String userId, Long workId, SettingBookCreateRequestDto request) {
        // 1. DB에 저장 (우선 저장)
        Integer[] epNumArray = null;
        if (request.getEpisode() != null) {
            epNumArray = request.getEpisode().toArray(new Integer[0]);
        }

        commandRepository.insert(
                userId,
                workId,
                request.getCategory(),
                request.getKeyword(),
                request.getSettings(),
                epNumArray
        );
        log.info("설정집 DB 저장 완료: WorkId={}, Keyword={}", workId, request.getKeyword());

        // 2. AI 서버로 비교 요청 준비 (연쇄 동작)
        // 요청 형식:
        // {
        //   "check": { "category": ["keyword"] },
        //   "user_id": "...",
        //   "work_id": "...",
        //   "category": { "keyword": "settings_content" }
        // }
        Map<String, Object> aiRequest = new HashMap<>();

        // 2-1. check 필드 생성
        Map<String, List<String>> checkMap = new HashMap<>();
        checkMap.put(request.getCategory(), List.of(request.getKeyword()));
        aiRequest.put("check", checkMap);

        // 2-2. 기본 ID 정보
        aiRequest.put("user_id", userId);
        aiRequest.put("work_id", workId);

        // 2-3. 실제 설정 내용 (카테고리를 키로 사용)
        Map<String, String> contentMap = new HashMap<>();
        contentMap.put(request.getKeyword(), request.getSettings());
        aiRequest.put(request.getCategory(), contentMap);

        // 3. AI 호출 및 결과 반환
        log.info("AI 설정집 비교 분석 요청 시작...");
        return aiLorebookClient.manualComparison(aiRequest);
    }

    @Override
    @Transactional
    public void update(Long id, String userId, SettingBookUpdateRequestDto request) {
        int updated = commandRepository.update(
                id,
                userId,
                request.getKeyword(),
                request.getSettings()
        );
        if (updated == 0) {
            throw new RuntimeException("수정할 설정집이 없거나 권한이 없습니다. ID: " + id);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        int deleted = commandRepository.delete(id);
        if (deleted == 0) {
            throw new RuntimeException("삭제할 설정집이 없거나 이미 삭제되었습니다. ID: " + id);
        }
    }

    @Override
    public List<Object> searchSimilarLore(String userId, Long workId, String query, String category) {
        String targetCategory = (category == null || category.isEmpty() || category.equals("all")) ? "*" : category;

        AiLorebookClient.LorebookSearchRequest request = AiLorebookClient.LorebookSearchRequest.builder()
                .category(targetCategory)
                .userQuery(query)
                .userId(userId)
                .workId(workId)
                .sim(0.3)
                .limit(5)
                .build();

        return aiLorebookClient.searchSimilarLore(request);
    }
}