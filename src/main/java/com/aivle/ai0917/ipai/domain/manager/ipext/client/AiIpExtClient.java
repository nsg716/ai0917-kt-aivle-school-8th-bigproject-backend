package com.aivle.ai0917.ipai.domain.manager.ipext.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiIpExtClient {

    private final WebClient aiWebClient;

    /**
     * 1. 설정집 충돌 검사 요청 (AI)
     * - AI 엔드포인트: POST /iplorebook
     */
    public LorebookCheckResponse checkLorebookConflict(List<Map<String, Object>> lorebooks) {
        log.info("AI 서버로 IP 확장 설정집 충돌 검사 요청: 로어북 개수={}", lorebooks.size());

        LorebookCheckRequest request = LorebookCheckRequest.builder()
                .lorebooks(lorebooks)
                .build();

        try {
            LorebookCheckResponse response = aiWebClient.post()
                    .uri("/iplorebook")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(LorebookCheckResponse.class)
                    .block();

            log.info("설정집 충돌 검사 완료");
            return response;

        } catch (Exception e) {
            log.error("설정집 충돌 검사 실패: {}", e.getMessage(), e);
            throw new RuntimeException("AI 서버 통신 오류 (충돌 검사): " + e.getMessage());
        }
    }

    /**
     * 2. IP 기획서 PDF 생성 및 전략 수립 요청 (AI)
     * - AI 엔드포인트: POST /ipproposal
     */
    public ProposalResponse createIpProposal(Long proposalId, List<Map<String, Object>> processedLorebooks) {
        log.info("AI 서버로 IP 기획서 생성 요청: ID={}, 로어북 데이터 유무={}",
                proposalId, (processedLorebooks != null && !processedLorebooks.isEmpty()));

        // ⭐ Map으로 직접 요청 데이터 구성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("id", proposalId);
        requestBody.put("processed_lorebooks", processedLorebooks);

        try {
            ProposalResponse response = aiWebClient.post()
                    .uri("/ipproposal")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)  // ⭐ Map을 JSON으로 변환하여 전송
                    .retrieve()
                    .bodyToMono(ProposalResponse.class)
                    .block();

            log.info("IP 기획서 생성 완료: PDF Path={}", response.getPdfPath());
            return response;

        } catch (WebClientResponseException e) {
            log.error("AI 서버 응답 오류: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI 서버 통신 오류 (기획서 생성): " + e.getMessage());
        } catch (Exception e) {
            log.error("IP 기획서 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("AI 서버 통신 오류 (기획서 생성): " + e.getMessage());
        }
    }
    // ===== DTO 정의 =====

    // 1. 충돌 검사 요청 DTO
    @Getter
    @Builder
    public static class LorebookCheckRequest {
        @JsonProperty("lorebooks")
        private List<Map<String, Object>> lorebooks;
    }

    // 1. 충돌 검사 응답 DTO
    @Getter
    @Setter
    public static class LorebookCheckResponse {
        // AI 서버의 "result" 필드 (충돌 분석 결과)
        @JsonProperty("result")
        private Map<String, Object> analysisResult;

        // AI 서버의 "processed_lorebooks" 필드 (전처리된 데이터 -> 프론트로 전달용)
        @JsonProperty("processed_lorebooks")
        private List<Map<String, Object>> processedLorebooks;
    }

    // 2. 기획서 생성 요청 DTO
    @Getter
    @Builder
    public static class IpProposalRequest {
        @JsonProperty("id")
        private Long id;

        // AI 서버가 받길 원하는 키 이름: "processed_lorebooks" (FastAPI 코드의 data.get("processed_lorebooks") 대응)
        @JsonProperty("processed_lorebooks")
        private List<Map<String, Object>> processedLorebooks;
    }

    // 2. 기획서 생성 응답 DTO
    @Getter
    @Setter
    public static class ProposalResponse {
        @JsonProperty("success")
        private boolean success;

        @JsonProperty("pdf_path")
        private String pdfPath;

        @JsonProperty("data")
        private Map<String, Object> data; // 전략 상세 내용
    }
}