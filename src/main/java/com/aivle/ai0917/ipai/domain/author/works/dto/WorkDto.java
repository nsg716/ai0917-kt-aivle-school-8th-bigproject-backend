package com.aivle.ai0917.ipai.domain.author.works.dto;

import com.aivle.ai0917.ipai.domain.author.works.model.WorkStatus; // Import 추가
import lombok.*;
import java.time.LocalDateTime;

public class WorkDto {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CreateRequest {
        private String title;
        private String synopsis;
        private String genre;
        private String coverImageUrl;
        private String primaryAuthorId;
        private Long universeId;
        // 생성 시 기본값은 Service나 DB에서 처리하므로 입력받지 않음
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class UpdateRequest {
        private String title;
        private String synopsis;
        private String genre;
        private String coverImageUrl;
    }

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private Long universeId;
        private String primaryAuthorId;
        private String title;
        private String synopsis;
        private String genre;
        private WorkStatus status; // [수정] String -> WorkStatus
        private String statusDescription; // [추가] 한글 설명용 (예: "연재중")
        private String coverImageUrl;
        private LocalDateTime createdAt;
    }
}