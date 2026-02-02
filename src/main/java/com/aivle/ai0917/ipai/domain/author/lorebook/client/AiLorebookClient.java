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

    /**
     * 4. 설정집 충돌 이후 업로드
     * POST /DB_insert
     */
    public String insertAfterConflict(DbInsertRequest request) {
        log.info("AI 서버로 충돌 해결 후 업로드 요청: WorkId={}, UserId={}", request.getWorkId(), request.getUserId());
        try {
            return aiWebClient.post()
                    .uri("/dbupsert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("충돌 해결 후 업로드 실패: {}", e.getMessage(), e);
            throw new RuntimeException("AI 서버 업로드 실패: " + e.getMessage());
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
        private Object conflicts; // [수정] List -> Object

        @JsonProperty("설정 결합")
        private Object settingMerge; // [수정] List -> Object

        @JsonProperty("신규 업로드")
        private Object newUploads; // [수정] List -> Object

        @JsonProperty("기존설정")
        private Object existingSettings; // [수정] List -> Object
    }
    // [추가됨] 누락되었던 DbInsertRequest DTO 정의
    @Getter
    @Setter
    @Builder
    public static class DbInsertRequest {
        @JsonProperty("work_id")
        private Long workId;

        @JsonProperty("user_id")
        private String userId;

        @JsonProperty("universe_id")
        private Long universeId;

        @JsonProperty("setting")
        private Object setting; // JSON 객체 전체를 담기 위해 Object 사용
    }


}