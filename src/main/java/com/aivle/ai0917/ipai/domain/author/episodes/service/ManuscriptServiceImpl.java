package com.aivle.ai0917.ipai.domain.author.episodes.service;

import com.aivle.ai0917.ipai.domain.author.episodes.client.AiAnalysisClient;
import com.aivle.ai0917.ipai.domain.author.episodes.client.AiManuscriptClient;
import com.aivle.ai0917.ipai.domain.author.episodes.dto.CategoryAnalysisRequestDto;
import com.aivle.ai0917.ipai.domain.author.episodes.dto.ManuscriptRequestDto;
import com.aivle.ai0917.ipai.domain.author.episodes.dto.ManuscriptResponseDto;
import com.aivle.ai0917.ipai.domain.author.episodes.dto.ManuscriptUpdateRequestDto;
import com.aivle.ai0917.ipai.domain.author.episodes.model.ManuscriptView;
import com.aivle.ai0917.ipai.domain.author.episodes.repository.ManuscriptCommandRepository;
import com.aivle.ai0917.ipai.domain.author.episodes.repository.ManuscriptRepository;
import com.aivle.ai0917.ipai.domain.author.works.model.Work;
import com.aivle.ai0917.ipai.domain.author.works.model.WorkStatus;
import com.aivle.ai0917.ipai.domain.author.works.repository.WorkCommandRepository;
import com.aivle.ai0917.ipai.domain.author.works.repository.WorkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation; // [중요] 추가
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
// [설정 1] 기본적으로 읽기 전용 트랜잭션을 사용하여 조회 성능 최적화
@Transactional(readOnly = true)
public class ManuscriptServiceImpl implements ManuscriptService {

    private final ManuscriptRepository manuscriptRepository;
    private final ManuscriptCommandRepository manuscriptCommandRepository;
    private final AiManuscriptClient aiManuscriptClient;
    private final AiAnalysisClient aiAnalysisClient;

    private final WorkRepository workRepository;
    private final WorkCommandRepository workCommandRepository;

    @Override
    public Page<ManuscriptResponseDto> getManuscriptList(
            String userId, String title, String keyword, Pageable pageable) {
        // 클래스 레벨의 readOnly = true가 적용됨
        Page<ManuscriptView> page = (keyword == null || keyword.isBlank())
                ? manuscriptRepository.findByUserIdAndTitle(userId, title, pageable)
                : manuscriptRepository.findByUserIdAndTitleAndTitleContaining(userId, title, keyword, pageable);

        return page.map(ManuscriptResponseDto::new);
    }

    @Override
    public ManuscriptResponseDto getManuscriptDetail(Long id) {
        // 클래스 레벨의 readOnly = true가 적용됨
        ManuscriptView manuscript = manuscriptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 원문을 찾을 수 없습니다. ID: " + id));

        String txt = aiManuscriptClient.readNovelFromAi(
                manuscript.getUserId(),
                manuscript.getWorkId(),
                manuscript.getEpisode()
        );

        return new ManuscriptResponseDto(manuscript, txt);
    }


    // [설정 2] 쓰기 작업이므로 기본 트랜잭션(readOnly=false) 적용
    @Override
    @Transactional
    public Long createManuscript(ManuscriptRequestDto request) {
        log.info("원문 신규 생성 요청: userId={}, workId={}", request.getUserId(), request.getWorkId());

        if (request.getEpisode() == null || request.getEpisode() == 0) {
            Integer maxEp = manuscriptRepository.findMaxEpisodeByWorkId(request.getWorkId());
            int nextEp = (maxEp == null) ? 1 : maxEp + 1;
            request.setEpisode(nextEp);
        }

        boolean exists = manuscriptRepository.findByWorkIdAndEpisode(
                request.getWorkId(), request.getEpisode()).isPresent();
        if (exists) {
            throw new IllegalStateException("이미 존재하는 회차입니다. 수정(Update) API를 이용해주세요.");
        }

        boolean hasPendingAnalysis = manuscriptRepository.existsByWorkIdAndIsReadOnlyFalse(request.getWorkId());
        if (hasPendingAnalysis) {
            log.warn("업로드 차단: 작품 ID {}에 분석 중인 에피소드가 존재합니다.", request.getWorkId());
            throw new IllegalStateException("이전 원고의 분석이 완료되지 않아 새로운 원고를 업로드할 수 없습니다.");
        }

        int wordCount = (request.getTxt() != null) ? request.getTxt().length() : 0;

        manuscriptCommandRepository.insert(
                request.getUserId(),
                request.getWorkId(),
                request.getTitle(),
                request.getEpisode(),
                request.getSubtitle(),
                "pending",
                wordCount
        );

        ManuscriptView savedManuscript = manuscriptRepository
                .findByUserIdAndTitle(request.getUserId(), request.getTitle(), Pageable.unpaged())
                .stream()
                .filter(m -> m.getEpisode().equals(request.getEpisode())
                        && m.getWorkId().equals(request.getWorkId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("저장된 원문을 찾을 수 없습니다."));

        Long episodeId = savedManuscript.getId();

        // 주의: AI 파일 저장이 오래 걸릴 경우 여기서도 병목이 생길 수 있음.
        // 하지만 create는 데이터 정합성이 중요하므로 Transaction 안에서 수행하는 것이 일반적임.
        processAiFileSave(episodeId, request);
        updateWorkStatusToOngoingIfNeeded(request.getWorkId());

        return episodeId;
    }

    @Override
    @Transactional
    public Long modifyManuscriptText(ManuscriptRequestDto request) {
        log.info("원문 내용 수정 요청: userId={}, workId={}, ep={}",
                request.getUserId(), request.getWorkId(), request.getEpisode());

        ManuscriptView existing = manuscriptRepository.findByWorkIdAndEpisode(
                        request.getWorkId(), request.getEpisode())
                .orElseThrow(() -> new RuntimeException("수정할 원문이 존재하지 않습니다."));

        Long episodeId = existing.getId();
        int wordCount = (request.getTxt() != null) ? request.getTxt().length() : 0;

        manuscriptCommandRepository.updateManuscript(
                episodeId,
                request.getSubtitle(),
                request.getEpisode(),
                null,
                wordCount
        );

        processAiFileSave(episodeId, request);

        return episodeId;
    }

    private void processAiFileSave(Long episodeId, ManuscriptRequestDto request) {
        String aiFilePath = aiManuscriptClient.saveNovelToAi(
                episodeId, request.getUserId(), request.getWorkId(), request.getEpisode(), request.getTxt());

        manuscriptCommandRepository.updateTxtPath(episodeId, aiFilePath);
    }


    // =========================================================================
    // [설정 3] 핵심 수정 구간: AI 통신 시 DB 트랜잭션을 타지 않도록 설정 (타임아웃 해결)
    // =========================================================================

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AiAnalysisClient.CategoryExtractionResponse extractCategories(
            String userId, CategoryAnalysisRequestDto requestDto) {

        log.info("카테고리 추출 시작(DB 트랜잭션 없음): workId={}, epNum={}",
                requestDto.getWorkId(), requestDto.getEpNum());

        AiAnalysisClient.CategoryExtractionRequest clientRequest =
                AiAnalysisClient.CategoryExtractionRequest.builder()
                        .userId(userId)
                        .workId(requestDto.getWorkId())
                        .epNum(requestDto.getEpNum())
                        .subtitle(requestDto.getSubtitle())
                        .build();

        // 여기서 시간이 오래 걸려도 DB 커넥션은 점유하지 않음
        AiAnalysisClient.CategoryExtractionResponse response =
                aiAnalysisClient.extractCategories(requestDto.getEpisodeId(), clientRequest);

        log.info("카테고리 추출 완료");
        return response;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AiAnalysisClient.SettingConflictResponse checkSettingConflict(
            Long workId, String userId, AiAnalysisClient.CategoryExtractionResponse categories) {

        log.info("설정집 충돌 검토 시작(DB 트랜잭션 없음): workId={}", workId);

        Map<String, List<String>> checkData = new HashMap<>();
        // ... (데이터 매핑 로직 동일) ...
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
                        .userId(userId)
                        .build();

        // 여기서 30초 이상 걸려도 HikariPool 에러가 발생하지 않음
        AiAnalysisClient.SettingConflictResponse response =
                aiAnalysisClient.checkSettingConflict(workId, request);

        log.info("설정집 충돌 검토 완료");
        return response;
    }

    // =========================================================================

    @Override
    @Transactional
    public void deleteManuscript(Long id) {
        ManuscriptView view = manuscriptRepository.findById(id).orElse(null);

        if (view == null) {
            throw new RuntimeException("삭제할 원문이 없거나 이미 삭제되었습니다.");
        }

        Long workId = view.getWorkId();
        Integer deletedEpNum = view.getEpisode();

        try {
            aiManuscriptClient.saveNovelToAi(
                    view.getId(), view.getUserId(), view.getWorkId(), view.getEpisode(), "");
        } catch (Exception e) {
            log.warn("원문 파일 내용 비우기 실패: {}", e.getMessage());
        }

        int deleted = manuscriptCommandRepository.deleteById(id);

        if (deleted > 0) {
            manuscriptCommandRepository.reorderEpisodesAfterDeletion(workId, deletedEpNum);
            revertWorkStatusToNewIfEmpty(workId);
        }
    }

    private void revertWorkStatusToNewIfEmpty(Long workId) {
        long count = manuscriptRepository.countByWorkId(workId);
        if (count == 0) {
            workCommandRepository.updateStatus(workId, WorkStatus.NEW.name());
        }
    }

    private void updateWorkStatusToOngoingIfNeeded(Long workId) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new RuntimeException("작품을 찾을 수 없습니다."));

        if (work.getStatus() == WorkStatus.NEW) {
            workCommandRepository.updateStatus(workId, WorkStatus.ONGOING.name());
        }
    }

    @Override
    @Transactional
    public void updateManuscript(Long id, ManuscriptUpdateRequestDto request) {
        ManuscriptView existing = manuscriptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("수정할 원문을 찾을 수 없습니다."));

        String newTxtPath = null;
        Integer newWordCount = null;

        if (request.getTxt() != null) {
            Integer targetEpisode = (request.getEpNum() != null) ? request.getEpNum() : existing.getEpisode();
            newTxtPath = aiManuscriptClient.saveNovelToAi(
                    existing.getId(), existing.getUserId(), existing.getWorkId(), targetEpisode, request.getTxt());
            newWordCount = request.getTxt().length();
        }

        int updated = manuscriptCommandRepository.updateManuscript(
                id, request.getSubtitle(), request.getEpNum(), newTxtPath, newWordCount);

        if (updated == 0) {
            throw new RuntimeException("원문 수정에 실패했습니다.");
        }
    }
}