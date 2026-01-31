package com.aivle.ai0917.ipai.domain.author.lorebook.client;

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
public class AiLorebookClient {

    private final WebClient aiWebClient;

    /**
     * 1. 설정집 유사도 검색 (AI)
     * POST /userQ
     */
    public List<Object> searchSimilarLore(LorebookSearchRequest request) {
        log.info("AI 서버로 유사도 검색 요청: Query={}, WorkId={}", request.getUserQuery(), request.getWorkId());
        try {
            return aiWebClient.post()
                    .uri("/userQ")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
        } catch (Exception e) {
            log.error("유사도 검색 실패: {}", e.getMessage(), e);
            throw new RuntimeException("AI 유사도 검색 실패: " + e.getMessage());
        }
    }

    /**
     * 2. 설정집 수동 비교 (AI) - [추가됨]
     * POST /comparison
     * 요청 바디에 카테고리명(예: "인물")이 동적 키로 들어가므로 Map 사용
     */
    public ManualComparisonResponse manualComparison(Map<String, Object> requestMap) {
        log.info("AI 서버로 수동 비교 요청: Data={}", requestMap);
        try {
            return aiWebClient.post()
                    .uri("/comparison")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestMap)
                    .retrieve()
                    .bodyToMono(ManualComparisonResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("수동 비교 분석 실패: {}", e.getMessage(), e);
            throw new RuntimeException("AI 수동 비교 분석 실패: " + e.getMessage());
        }
    }

    /**
     * 3. 임베딩 벡터 생성 (AI)
     * POST /get_vector
     */
    public List<Double> getVector(String text) {
        try {
            return aiWebClient.post()
                    .uri("/get_vector")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("original", text))
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
        } catch (Exception e) {
            log.error("벡터 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("벡터 생성 실패: " + e.getMessage());
        }
    }

    // ===== DTO 정의 =====

    @Getter
    @Setter
    @Builder
    public static class LorebookSearchRequest {
        @JsonProperty("category")
        private String category;
        @JsonProperty("user_query")
        private String userQuery;
        @JsonProperty("user_id")
        private String userId;
        @JsonProperty("work_id")
        private Long workId;
        @JsonProperty("sim")
        private Double sim;
        @JsonProperty("limit")
        private Integer limit;
    }

    // [추가] 수동 비교 응답 DTO
    @Getter
    @Setter
    public static class ManualComparisonResponse {
        @JsonProperty("충돌")
        private List<Object> conflicts; // [{인물1:충돌사유, "신규설정":...}, ...]

        @JsonProperty("설정 결합")
        private List<Object> settingMerge; // [[lore_id, {결합 설정집}, ep_nums], ...]

        @JsonProperty("신규 업로드")
        private List<Object> newUploads; // [{설정집}, ep_num]

        @JsonProperty("기존설정")
        private List<Object> existingSettings; // [[lore_id, setting, ep_nums], ...]
    }
}