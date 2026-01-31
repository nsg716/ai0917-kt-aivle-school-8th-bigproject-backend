package com.aivle.ai0917.ipai.domain.author.episodes.service;

import com.aivle.ai0917.ipai.domain.author.episodes.client.AiAnalysisClient;
import com.aivle.ai0917.ipai.domain.author.episodes.client.AiManuscriptClient;
import com.aivle.ai0917.ipai.domain.author.episodes.dto.CategoryAnalysisRequestDto;
import com.aivle.ai0917.ipai.domain.author.episodes.dto.ManuscriptRequestDto;
import com.aivle.ai0917.ipai.domain.author.episodes.dto.ManuscriptResponseDto;
import com.aivle.ai0917.ipai.domain.author.episodes.model.ManuscriptView;
import com.aivle.ai0917.ipai.domain.author.episodes.repository.ManuscriptCommandRepository;
import com.aivle.ai0917.ipai.domain.author.episodes.repository.ManuscriptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManuscriptServiceImpl implements ManuscriptService {

    private final ManuscriptRepository manuscriptRepository;
    private final ManuscriptCommandRepository manuscriptCommandRepository;
    private final AiManuscriptClient aiManuscriptClient;
    private final AiAnalysisClient aiAnalysisClient;

    @Override
    public Page<ManuscriptResponseDto> getManuscriptList(
            String userId, String title, String keyword, Pageable pageable) {

        Page<ManuscriptView> page = (keyword == null || keyword.isBlank())
                ? manuscriptRepository.findByUserIdAndTitle(userId, title, pageable)
                : manuscriptRepository.findByUserIdAndTitleAndTitleContaining(userId, title, keyword, pageable);

        return page.map(ManuscriptResponseDto::new);
    }

    @Override
    public ManuscriptResponseDto getManuscriptDetail(Long id) {
        ManuscriptView manuscript = manuscriptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 원문을 찾을 수 없습니다. ID: " + id));

        String txt = aiManuscriptClient.readNovelFromAi(
                manuscript.getUserId(),
                manuscript.getWorkId(),
                manuscript.getEpisode()
        );

        return new ManuscriptResponseDto(manuscript, txt);
    }

    @Override
    @Transactional
    public Long uploadManuscript(ManuscriptRequestDto request) {
        log.info("원문 업로드 요청(Text): userId={}, workId={}, episode={}",
                request.getUserId(), request.getWorkId(), request.getEpisode());

        // 글자 수 계산
        int wordCount = (request.getTxt() != null) ? request.getTxt().length() : 0;

        // 1. DB에 메타데이터 저장
        manuscriptCommandRepository.insert(
                request.getUserId(),
                request.getWorkId(),
                request.getTitle(),
                request.getEpisode(),
                request.getSubtitle(),
                "pending",
                wordCount
        );

        // 2. 생성된 ID 조회
        ManuscriptView savedManuscript = manuscriptRepository
                .findByUserIdAndTitle(request.getUserId(), request.getTitle(), Pageable.unpaged())
                .stream()
                .filter(m -> m.getEpisode().equals(request.getEpisode())
                        && m.getWorkId().equals(request.getWorkId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("저장된 원문을 찾을 수 없습니다."));

        Long episodeId = savedManuscript.getId();

        // 3. AI 서버로 텍스트 전송
        String aiFilePath = aiManuscriptClient.saveNovelToAi(
                episodeId,
                request.getUserId(),
                request.getWorkId(),
                request.getEpisode(),
                request.getTxt()
        );

        // 4. DB 업데이트
        manuscriptCommandRepository.updateTxtPath(episodeId, aiFilePath);

        log.info("원문 업로드 및 AI 전송 완료: episodeId={}, wordCount={}", episodeId, wordCount);
        return episodeId;
    }

    @Override
    public AiAnalysisClient.CategoryExtractionResponse extractCategories(
            String userId, CategoryAnalysisRequestDto requestDto) {

        log.info("카테고리 추출 시작: episodeId={}, workId={}, epNum={}",
                requestDto.getEpisodeId(), requestDto.getWorkId(), requestDto.getEpNum());

        AiAnalysisClient.CategoryExtractionRequest clientRequest =
                AiAnalysisClient.CategoryExtractionRequest.builder()
                        .userId(userId)
                        .workId(requestDto.getWorkId())
                        .epNum(requestDto.getEpNum())
                        .subtitle(requestDto.getSubtitle())
                        .build();

        AiAnalysisClient.CategoryExtractionResponse response =
                aiAnalysisClient.extractCategories(requestDto.getEpisodeId(), clientRequest);

        log.info("카테고리 추출 완료");
        return response;
    }

    @Override
    public AiAnalysisClient.SettingConflictResponse checkSettingConflict(
            Long workId, String userId, AiAnalysisClient.CategoryExtractionResponse categories) {

        log.info("설정집 충돌 검토 시작: workId={}, userId={}", workId, userId);

        Map<String, List<String>> checkData = new HashMap<>();

        checkData.put("인물", categories.getCharacters() != null ? categories.getCharacters() : List.of());
        checkData.put("세계", categories.getWorldRules() != null ? categories.getWorldRules() : List.of());
        checkData.put("장소", categories.getLocations() != null ? categories.getLocations() : List.of());
        checkData.put("사건", categories.getEvents() != null ? categories.getEvents() : List.of());
        checkData.put("물건", categories.getItems() != null ? categories.getItems() : List.of());
        checkData.put("집단", categories.getGroups() != null ? categories.getGroups() : List.of());

        AiAnalysisClient.SettingConflictRequest request =
                AiAnalysisClient.SettingConflictRequest.builder()
                        .check(checkData)
                        .workId(workId)
                        .userId(userId) // [수정] user_id 추가
                        .build();

        AiAnalysisClient.SettingConflictResponse response =
                aiAnalysisClient.checkSettingConflict(workId, request);

        log.info("설정집 충돌 검토 완료");
        return response;
    }

    @Override
    @Transactional
    public void deleteManuscript(Long id) {
        ManuscriptView view = manuscriptRepository.findById(id).orElse(null);
        int deleted = manuscriptCommandRepository.deleteById(id);
        if (deleted == 0) {
            throw new RuntimeException("삭제할 원문이 없거나 이미 삭제되었습니다.");
        }
        if (view != null) {
            log.info("원문 삭제 완료. ID: {}", id);
        }
    }
}