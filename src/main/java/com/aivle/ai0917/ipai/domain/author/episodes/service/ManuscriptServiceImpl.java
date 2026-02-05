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
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
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

        boolean hasPendingAnalysis = manuscriptRepository.existsByWorkIdAndIsReadOnlyFalse(request.getWorkId());

        if (hasPendingAnalysis) {
            log.warn("업로드 차단: 작품 ID {}에 분석 중인(is_read_only=false) 에피소드가 존재합니다.", request.getWorkId());
            throw new IllegalStateException("이전 원고의 분석이 완료되지 않아 새로운 원고를 업로드할 수 없습니다.");
        }

        log.info("원문 업로드 요청: userId={}, workId={}", request.getUserId(), request.getWorkId());

        // [추가 로직] 회차(Episode)가 없거나 0이면 자동 증가 처리
        if (request.getEpisode() == null || request.getEpisode() == 0) {
            Integer maxEp = manuscriptRepository.findMaxEpisodeByWorkId(request.getWorkId());
            // 기존 원고가 없으면 1화, 있으면 +1화
            int nextEp = (maxEp == null) ? 1 : maxEp + 1;
            request.setEpisode(nextEp);
            log.info("회차 번호 자동 할당: {}화", nextEp);
        }

        // 1. 글자 수 계산
        int wordCount = (request.getTxt() != null) ? request.getTxt().length() : 0;

        // 2. 이미 존재하는 회차인지 확인 (Upsert 로직)
        Optional<ManuscriptView> existingOpt = manuscriptRepository.findByWorkIdAndEpisode(
                request.getWorkId(), request.getEpisode());

        Long episodeId;

        if (existingOpt.isPresent()) {
            ManuscriptView existing = existingOpt.get();
            episodeId = existing.getId();

            manuscriptCommandRepository.updateManuscript(
                    episodeId,
                    request.getSubtitle(),
                    request.getEpisode(),
                    null,
                    wordCount
            );
            log.info("기존 원문 덮어쓰기(Update): ID={}", episodeId);
        } else {
            manuscriptCommandRepository.insert(
                    request.getUserId(),
                    request.getWorkId(),
                    request.getTitle(),
                    request.getEpisode(),
                    request.getSubtitle(),
                    "pending",
                    wordCount
            );

            // 방금 생성된 ID 조회 로직 (기존과 동일)
            ManuscriptView savedManuscript = manuscriptRepository
                    .findByUserIdAndTitle(request.getUserId(), request.getTitle(), Pageable.unpaged())
                    .stream()
                    .filter(m -> m.getEpisode().equals(request.getEpisode())
                            && m.getWorkId().equals(request.getWorkId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("저장된 원문을 찾을 수 없습니다."));

            episodeId = savedManuscript.getId();
            log.info("신규 원문 등록(Insert): ID={}", episodeId);
        }
        // 3. AI 서버 전송 및 4. 경로 업데이트 (기존과 동일)
        String aiFilePath = aiManuscriptClient.saveNovelToAi(
                episodeId, request.getUserId(), request.getWorkId(), request.getEpisode(), request.getTxt());
        manuscriptCommandRepository.updateTxtPath(episodeId, aiFilePath);
        updateWorkStatusToOngoingIfNeeded(request.getWorkId());

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
        // 1. 삭제 대상 정보 조회
        ManuscriptView view = manuscriptRepository.findById(id).orElse(null);

        if (view == null) {
            throw new RuntimeException("삭제할 원문이 없거나 이미 삭제되었습니다.");
        }

        Long workId = view.getWorkId();
        Integer deletedEpNum = view.getEpisode();

        // [추가] 2. 실제 파일 내용 비우기 (빈 문자열 "" 전송)
        // DB의 txt_path는 유지되지만, 그 경로에 있는 파일 내용은 빈 깡통이 됩니다.
        try {
            aiManuscriptClient.saveNovelToAi(
                    view.getId(),
                    view.getUserId(),
                    view.getWorkId(),
                    view.getEpisode(),
                    "" // 빈 문자열 전송 -> 파일 내용 삭제 효과
            );
            log.info("원문 파일 내용 초기화 완료 (ID: {})", id);
        } catch (Exception e) {
            // 파일 비우기 실패해도 DB 삭제는 진행할지, 멈출지 결정해야 함.
            // 보통은 로그만 남기고 DB 삭제 진행
            log.warn("원문 파일 내용 비우기 실패 (ID: {}): {}", id, e.getMessage());
        }

        // 3. DB Soft Delete (txt_path는 유지, word_count=0)
        int deleted = manuscriptCommandRepository.deleteById(id);

        if (deleted > 0) {
            log.info("원문 삭제(Soft Delete) 완료. ID: {}, Ep: {}", id, deletedEpNum);

            // 4. 번호 재정렬
            manuscriptCommandRepository.reorderEpisodesAfterDeletion(workId, deletedEpNum);
            log.info("회차 재정렬 완료 (workId={}, deletedEp={})", workId, deletedEpNum);

            // 5. 작품 상태 변경 체크
            revertWorkStatusToNewIfEmpty(workId);
        }
    }

    private void revertWorkStatusToNewIfEmpty(Long workId) {
        // 남은 원문 개수 조회 (active_episodes_view 기준이므로 삭제된 건 카운트 안 됨)
        long count = manuscriptRepository.countByWorkId(workId);

        if (count == 0) {
            // 상태를 NEW로 업데이트
            workCommandRepository.updateStatus(workId, WorkStatus.NEW.name());
            log.info("남은 원문이 없어 작품 상태 변경 (ONGOING -> NEW): workId={}", workId);
        }
    }

    // [추가] 작품 상태 변경 헬퍼 메서드
    private void updateWorkStatusToOngoingIfNeeded(Long workId) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new RuntimeException("작품을 찾을 수 없습니다. WorkID: " + workId));

        if (work.getStatus() == WorkStatus.NEW) {
            workCommandRepository.updateStatus(workId, WorkStatus.ONGOING.name());
            log.info("첫 원문 등록으로 작품 상태 변경 (NEW -> ONGOING): workId={}", workId);
        }
    }
    @Override
    @Transactional
    public void updateManuscript(Long id, ManuscriptUpdateRequestDto request) {
        // 1. 기존 원문 조회
        ManuscriptView existing = manuscriptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("수정할 원문을 찾을 수 없습니다. ID: " + id));

        String newTxtPath = null;
        Integer newWordCount = null;

        // 2. 텍스트 내용 변경 요청이 있는 경우 처리
        if (request.getTxt() != null) {
            // 회차(epNum)가 변경되었을 수 있으므로 request의 epNum을 우선 사용
            Integer targetEpisode = (request.getEpNum() != null) ? request.getEpNum() : existing.getEpisode();

            newTxtPath = aiManuscriptClient.saveNovelToAi(
                    existing.getId(),
                    existing.getUserId(),
                    existing.getWorkId(),
                    targetEpisode,
                    request.getTxt()
            );

            newWordCount = request.getTxt().length();
            log.info("원문 텍스트 파일 갱신 및 경로 확보: {}", newTxtPath);
        }

        // 3. DB 업데이트 실행 (5개 파라미터 버전 사용)
        int updated = manuscriptCommandRepository.updateManuscript(
                id,
                request.getSubtitle(),
                request.getEpNum(),
                newTxtPath,   // null이면 기존값 유지
                newWordCount  // null이면 기존값 유지
        );

        if (updated == 0) {
            throw new RuntimeException("원문 수정에 실패했습니다.");
        }

        log.info("원문 정보 수정 완료. ID={}, Subtitle={}, EpNum={}, TxtUpdated={}",
                id, request.getSubtitle(), request.getEpNum(), (newTxtPath != null));
    }
}