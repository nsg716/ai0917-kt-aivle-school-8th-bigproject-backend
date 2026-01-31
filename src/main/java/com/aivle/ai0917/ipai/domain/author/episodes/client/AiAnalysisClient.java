package com.aivle.ai0917.ipai.domain.author.episodes.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiAnalysisClient {

    private final WebClient aiWebClient;

    /**
     * 카테고리 추출 요청 (AI)
     */
    public CategoryExtractionResponse extractCategories(Long episodeId, CategoryExtractionRequest request) {
        log.info("AI 서버로 카테고리 추출 요청: EpisodeId={}, WorkId={}, Ep={}",
                episodeId, request.getWorkId(), request.getEpNum());
        try {
            CategoryExtractionResponse response = aiWebClient.post()
                    .uri("/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(CategoryExtractionResponse.class)
                    .block();
            log.info("카테고리 추출 완료");
            return response;
        } catch (Exception e) {
            log.error("카테고리 추출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("카테고리 추출 실패: " + e.getMessage());
        }
    }

    /**
     * 설정집 충돌 검토 요청 (AI)
     */
    public SettingConflictResponse checkSettingConflict(Long workId, SettingConflictRequest request) {
        log.info("AI 서버로 설정집 충돌 검토 요청: WorkId={}, UserId={}", workId, request.getUserId());
        try {
            SettingConflictResponse response = aiWebClient.post()
                    .uri("/setting")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(SettingConflictResponse.class)
                    .block();
            log.info("설정집 충돌 검토 완료");
            return response;
        } catch (Exception e) {
            log.error("설정집 충돌 검토 실패: {}", e.getMessage(), e);
            throw new RuntimeException("설정집 충돌 검토 실패: " + e.getMessage());
        }
    }

    // ===== DTO 정의 =====

    @Getter
    @Setter
    @Builder
    public static class CategoryExtractionRequest {
        @JsonProperty("ep_num")
        private Integer epNum;

        @JsonProperty("subtitle")
        private String subtitle;

        @JsonProperty("work_id")
        private Long workId;

        @JsonProperty("user_id")
        private String userId;
    }

    @Getter
    @Setter
    public static class CategoryExtractionResponse {
        @JsonProperty("인물")
        private List<String> characters;
        @JsonProperty("세계")
        private List<String> worldRules;
        @JsonProperty("장소")
        private List<String> locations;
        @JsonProperty("사건")
        private List<String> events;
        @JsonProperty("물건")
        private List<String> items;
        @JsonProperty("집단")
        private List<String> groups;
    }

    @Getter
    @Setter
    @Builder
    public static class SettingConflictRequest {
        @JsonProperty("check")
        private Map<String, List<String>> check;

        @JsonProperty("work_id")
        private Long workId;

        // [수정] user_id 필드 추가
        @JsonProperty("user_id")
        private String userId;
    }

    @Getter
    @Setter
    public static class SettingConflictResponse {
        @JsonProperty("충돌")
        private List<Map<String, String>> conflicts;
        @JsonProperty("설정 결합")
        private List<Map<String, Object>> settingMerge;
        @JsonProperty("신규 업로드")
        private List<Map<String, Object>> newUploads;
        @JsonProperty("기존설정")
        private List<Object> existingSettings;
    }
}