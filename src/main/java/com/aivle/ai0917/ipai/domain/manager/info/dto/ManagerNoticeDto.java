package com.aivle.ai0917.ipai.domain.manager.info.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ManagerNoticeDto {

    private Long id;
    private ManagerNoticeSource source;
    private String title;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
    private String redirectUrl;

    /**
     * 매니저용 알림 소스 정의
     */
    public enum ManagerNoticeSource {

        AUTHOR_PROPOSAL("작가 연결"), // 작가 매칭/제안 관련
        IP_EXTREND("트랜드 분석"),   // 트렌드 분석 결과 알림
        IP_EXT("IP 확장");         // IP 확장(OSMU) 제안/결과 알림

        private final String description;

        ManagerNoticeSource(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}