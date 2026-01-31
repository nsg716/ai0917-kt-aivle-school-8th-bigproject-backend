package com.aivle.ai0917.ipai.domain.author.episodes.client;

import com.aivle.ai0917.ipai.domain.author.episodes.dto.NovelSaveResponseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiManuscriptClient {

    private final WebClient aiWebClient;

    /**
     * AI 서버로 원문 저장 (Storage1 원문 저장)
     * POST /novel_save
     *
     * @return AI 서버에 저장된 파일 경로
     */
    public String saveNovelToAi(Long episodeId, String userId, Long workId, Integer epNum, String txt) {
        AiNovelSaveRequest request = AiNovelSaveRequest.builder()
                .userId(userId)
                .workId(workId)
                .epNum(epNum)
                .txt(txt)
                .build();

        log.info("AI 서버로 원문 저장 요청: EpisodeId={}, WorkId={}, Ep={}", episodeId, workId, epNum);

        try {
            // [수정] 경로를 "/novel_save"로 단순화 (Prefix 제거)
            String response = aiWebClient.post()
                    .uri("/novel_save")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("AI 서버 원문 저장 완료: {}", response);
            return response; // 저장 경로 반환

        } catch (Exception e) {
            log.error("AI 서버 원문 저장 실패: {}", e.getMessage(), e);
            throw new RuntimeException("원문 저장 실패: " + e.getMessage());
        }
    }

    /**
     * AI 서버에서 원문 읽기 (Storage1 원문 읽기)
     * GET /novel_read
     */
    public String readNovelFromAi(String userId, Long workId, Integer epNum) {
        log.info("AI 서버에서 원문 읽기 요청: WorkId={}, Ep={}", workId, epNum);

        try {
            // [수정] 경로를 "/novel_read"로 단순화 (Prefix 제거)
            String response = aiWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/novel_read")
                            .queryParam("user_id", userId)
                            .queryParam("work_id", workId)
                            .queryParam("ep_num", epNum)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("AI 서버 원문 읽기 완료");
            return response;

        } catch (Exception e) {
            log.error("AI 서버 원문 읽기 실패: {}", e.getMessage(), e);
            throw new RuntimeException("원문 읽기 실패: " + e.getMessage());
        }
    }

    @Getter
    @Builder
    private static class AiNovelSaveRequest {
        @JsonProperty("user_id")
        private String userId;

        @JsonProperty("work_id")
        private Long workId;

        @JsonProperty("ep_num")
        private Integer epNum;

        @JsonProperty("txt")
        private String txt;
    }
}