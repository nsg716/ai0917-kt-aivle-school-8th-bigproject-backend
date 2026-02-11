package com.aivle.ai0917.ipai.domain.author.analyze.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiGraphClient {

    private final WebClient aiWebClient;

    /**
     * 인물 관계도 분석 요청
     * POST /relationship
     */
    public Object requestRelationshipAnalysis(Long workId, String userId, String target) {
        RelationshipRequest request = RelationshipRequest.builder()
                .workId(workId)
                .userId(userId)
                .target(target)
                .build();

        log.info("AI(Graph) 인물관계 요청: WorkId={}, Target={}", workId, target);

        try {
            return aiWebClient.post()
                    .uri("/relationship")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (Exception e) {
            log.error("AI 인물관계 요청 실패: {}", e.getMessage());
            throw new RuntimeException("인물관계 분석 실패");
        }
    }

    /**
     * 사건 타임라인 분석 요청
     * POST /timeline
     */
    public Object requestTimelineAnalysis(Long workId, String userId, List<Integer> target) {
        TimelineRequest request = TimelineRequest.builder()
                .workId(workId)
                .userId(userId)
                .target(target)
                .build();

        log.info("AI(Graph) 타임라인 요청: WorkId={}, TargetCount={}", workId, target.size());

        try {
            return aiWebClient.post()
                    .uri("/timeline")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (Exception e) {
            log.error("AI 타임라인 요청 실패: {}", e.getMessage());
            throw new RuntimeException("타임라인 분석 실패");
        }
    }

    // --- DTO ---

    @Getter
    @Builder
    public static class RelationshipRequest {
        @JsonProperty("work_id")
        private Long workId;
        @JsonProperty("user_id")
        private String userId;
        @JsonProperty("target")
        private String target; // '*' or 'keyword'
    }

    @Getter
    @Builder
    public static class TimelineRequest {
        @JsonProperty("work_id")
        private Long workId;
        @JsonProperty("user_id")
        private String userId;
        @JsonProperty("target")
        private List<Integer> target; // [1, 2, 3...]
    }
}