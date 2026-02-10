package com.aivle.ai0917.ipai.domain.author.lorebook.service;

import com.aivle.ai0917.ipai.domain.author.episodes.repository.ManuscriptCommandRepository;
import com.aivle.ai0917.ipai.domain.author.lorebook.client.AiLorebookClient;
import com.aivle.ai0917.ipai.domain.author.lorebook.dto.*;
import com.aivle.ai0917.ipai.domain.author.lorebook.repository.SettingBookCommandRepository;
import com.aivle.ai0917.ipai.domain.author.lorebook.repository.SettingBookViewRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ManuscriptCommandRepository manuscriptCommandRepository;
    private final ObjectMapper objectMapper;

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

        // 1. 설정 내용(JSON String)을 Map으로 변환
        Map<String, Object> settingMap = parseSettingJson(request.getSettings());

        // 2. AI 서버로 수동 저장 요청 (DB 저장을 AI 서버에 위임)
        AiLorebookClient.ManualLorebookRequest insertRequest = AiLorebookClient.ManualLorebookRequest.builder()
                .workId(workId)
                .userId(userId)
                .keyword(request.getKeyword())
                .category(request.getCategory())
                .epNum(request.getEpisode()) // List<Integer> 그대로 전달
                .setting(settingMap)
                .build();

        aiLorebookClient.manualInsert(insertRequest);
        log.info("AI 서버를 통한 설정집 저장 완료: Keyword={}", request.getKeyword());
        return new AiLorebookClient.ManualComparisonResponse();
    }


    @Override
    @Transactional
    public void update(Long id, String userId, Long workId, SettingBookUpdateRequestDto request) { // [수정] workId 추가

        // 1. 설정 내용 파싱
        Map<String, Object> settingMap = parseSettingJson(request.getSettings());

        // 2. AI 서버로 수동 수정 요청
        AiLorebookClient.ManualLorebookRequest updateRequest = AiLorebookClient.ManualLorebookRequest.builder()
                .loreId(id)
                .userId(userId)
                .workId(workId) // [수정] 이제 전달받은 workId를 여기에 넣습니다.
                .keyword(request.getKeyword())
                .category(request.getCategory())
                .epNum(request.getEpisode())
                .setting(settingMap)
                .build();

        aiLorebookClient.manualUpdate(updateRequest);
        log.info("AI 서버를 통한 설정집 수정 완료: ID={}, WorkID={}", id, workId);
    }
//    @Override
//    @Transactional
//    public AiLorebookClient.ManualComparisonResponse create(String userId, Long workId, SettingBookCreateRequestDto request) {
//        // 1. DB에 저장 (우선 저장)
//        Integer[] epNumArray = null;
//        if (request.getEpisode() != null) {
//            epNumArray = request.getEpisode().toArray(new Integer[0]);
//        }
//
//        commandRepository.insert(
//                userId,
//                workId,
//                request.getCategory(),
//                request.getKeyword(),
//                request.getSettings(),
//                epNumArray
//        );
//        log.info("설정집 DB 저장 완료: WorkId={}, Keyword={}", workId, request.getKeyword());
//
//        // 2. AI 서버로 비교 요청 준비 (연쇄 동작)
//        // 요청 형식:
//        // {
//        //   "check": { "category": ["keyword"] },
//        //   "user_id": "...",
//        //   "work_id": "...",
//        //   "category": { "keyword": "settings_content" }
//        // }
//        Map<String, Object> aiRequest = new HashMap<>();
//
//        // 2-1. check 필드 생성
//        Map<String, List<String>> checkMap = new HashMap<>();
//        checkMap.put(request.getCategory(), List.of(request.getKeyword()));
//        aiRequest.put("check", checkMap);
//
//        // 2-2. 기본 ID 정보
//        aiRequest.put("user_id", userId);
//        aiRequest.put("work_id", workId);
//
//        // 2-3. 실제 설정 내용 (카테고리를 키로 사용)
//        Map<String, String> contentMap = new HashMap<>();
//        contentMap.put(request.getKeyword(), request.getSettings());
//        aiRequest.put(request.getCategory(), contentMap);
//
//        // 3. AI 호출 및 결과 반환
//        log.info("AI 설정집 비교 분석 요청 시작...");
//        return aiLorebookClient.manualComparison(aiRequest);
//    }

//    @Override
//    @Transactional
//    public void update(Long id, String userId, SettingBookUpdateRequestDto request) {
//        int updated = commandRepository.update(
//                id,
//                userId,
//                request.getKeyword(),
//                request.getSettings()
//        );
//        if (updated == 0) {
//            throw new RuntimeException("수정할 설정집이 없거나 권한이 없습니다. ID: " + id);
//        }
//    }

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
                .sim((double) 0)
                .limit(5)
                .build();

        return aiLorebookClient.searchSimilarLore(request);
    }

    @Override
    @Transactional
    public String saveAfterConflict(Long workId, String userId, Long universeId, Object settingJson, int episodeId) {

        // 1. AI 서버 전송 객체 생성 (DbInsertRequest)
        // [중요] 여기에 episodeId를 넣지 않습니다. 따라서 AI 서버로 전송되지 않습니다.
        AiLorebookClient.DbInsertRequest request = AiLorebookClient.DbInsertRequest.builder()
                .workId(workId)
                .userId(userId)
                .universeId(universeId)
                .setting(settingJson) // 설정 데이터만 전송
                .build();

        log.info("충돌 해결 데이터 AI 전송 시작");
        String response = aiLorebookClient.insertAfterConflict(request);
        log.info("충돌 해결 데이터 AI 전송 완료: {}", response);

        // 2. 에피소드 잠금 처리 (DB 작업)
        // int -> Long 변환 후 리스트로 감싸서 처리 (기존 쿼리 재사용)
        if (episodeId > 0) {
            manuscriptCommandRepository.updateIsReadOnlyTrue(List.of((long) episodeId));
            log.info("에피소드 잠금 처리 완료 (ID: {}, is_read_only=true)", episodeId);
        } else {
            log.warn("유효하지 않은 에피소드 ID입니다: {}", episodeId);
        }

        return response;
    }

    private Map<String, Object> parseSettingJson(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, Map.class);
        } catch (JsonProcessingException e) {
            log.error("설정 JSON 파싱 오류: {}", e.getMessage());
            // 파싱 실패 시, 단순 텍스트로라도 보내기 위해 래핑하거나 에러 처리
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("content", jsonString);
            return fallback;
        }
    }
}