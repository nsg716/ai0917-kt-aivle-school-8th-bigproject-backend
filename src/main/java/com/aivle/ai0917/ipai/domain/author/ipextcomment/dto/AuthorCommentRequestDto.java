package com.aivle.ai0917.ipai.domain.author.ipextcomment.dto;

import lombok.*;

@Getter
@Setter // RequestBody 바인딩용
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorCommentRequestDto {
    private Long proposalId;
    private String authorIntegrationId; // 작가 ID (로그인 정보에서 가져오는 것을 권장하나 요청에 포함)
    private String status;              // 승인(APPROVED) / 반려(REJECTED) / 대기(PENDING)
    private String comment;             // 의견 텍스트
}