package com.aivle.ai0917.ipai.domain.author.analyze.service;

import com.aivle.ai0917.ipai.domain.author.analyze.client.AiGraphClient;
import com.aivle.ai0917.ipai.domain.author.analyze.dto.EpisodeBriefDto;
import com.aivle.ai0917.ipai.domain.author.analyze.repository.ManuscriptViewRepository;
import com.aivle.ai0917.ipai.domain.author.episodes.model.ManuscriptView;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalyzeServiceImpl implements AiAnalyzeService {

    private final AiGraphClient aiGraphClient;
    private final ManuscriptViewRepository manuscriptViewRepository;

    @Override
    public Object analyzeRelationship(Long workId, String userId, String target) {
        // [로직 추가] target이 null이거나 공백이면 전체('*')로 설정
        String safeTarget = (target == null || target.trim().isEmpty()) ? "*" : target;

        log.info("ServiceImpl: 인물 관계 분석 실행 workId={}, userId={}, target={}(converted)", workId, userId, safeTarget);

        // FastAPI Client에는 변환된 safeTarget 전달
        return aiGraphClient.requestRelationshipAnalysis(workId, userId, safeTarget);
    }

    @Override
    public Object analyzeTimeline(Long workId, String userId, List<Integer> targetList) {
        log.info("ServiceImpl: 타임라인 분석 실행 workId={}, userId={}, targetSize={}",
                workId, userId, (targetList != null ? targetList.size() : 0));
        return aiGraphClient.requestTimelineAnalysis(workId, userId, targetList);
    }

    @Override
    @Transactional
    public List<EpisodeBriefDto> getTimelineEpisodes(Long workId) {
        log.info("ServiceImpl: 타임라인 원문 목록 조회 (ReadOnly Only) workId={}", workId);

        // workId가 일치하고 is_read_only가 true인 항목 조회
        List<ManuscriptView> episodes = manuscriptViewRepository.findByWorkIdAndIsReadOnlyTrueOrderByEpisodeAsc(workId);

        return episodes.stream()
                .map(EpisodeBriefDto::from)
                .collect(Collectors.toList());
    }
}