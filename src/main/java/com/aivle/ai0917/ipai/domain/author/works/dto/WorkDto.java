//package com.aivle.ai0917.ipai.domain.author.works.dto;
//
//import com.aivle.ai0917.ipai.domain.author.works.model.Work;
//import com.aivle.ai0917.ipai.domain.author.works.model.WorkStatus;
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//public class WorkDto {
//
//    @Getter
//    @NoArgsConstructor
//    public static class CreateRequest {
//        private String title;
//        private String userIntegrationId;
//        private String writer;
//        private String description;
//        private WorkStatus status;
//
//        public Work toEntity() {
//            return Work.builder()
//                    .title(title)
//                    .userIntegrationId(userIntegrationId)
//                    .writer(writer)
//                    .description(description)
//                    .status(status)
//                    .build();
//        }
//    }
//
//    @Getter
//    @NoArgsConstructor
//    public static class UpdateRequest {
//        private Long id; // 수정 시 식별자
//        private String title;
//        private String description;
//        private WorkStatus status;
//    }
//
//    @Getter
//    @Builder
//    public static class Response {
//        private Long id;
//        private String title;
//        private String writer;
//        private String description;
//        private WorkStatus status;
//        private String statusDescription;
//        private LocalDateTime createdAt;
//
//        public static Response from(Work work) {
//            return Response.builder()
//                    .id(work.getId())
//                    .title(work.getTitle())
//                    .writer(work.getWriter())
//                    .description(work.getDescription())
//                    .status(work.getStatus())
//                    .statusDescription(work.getStatus().getDescription())
//                    .createdAt(work.getCreatedAt())
//                    .build();
//        }
//    }
//}