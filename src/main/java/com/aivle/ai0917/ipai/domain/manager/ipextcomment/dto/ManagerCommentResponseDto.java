package com.aivle.ai0917.ipai.domain.manager.ipextcomment.dto;

import com.aivle.ai0917.ipai.domain.author.ipextcomment.model.IpProposalComment;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ManagerCommentResponseDto {
    private Long commentId;
    private String authorName;      // 작가 이름 (이정민)
    private String authorId;        // 작가 ID (eZHXUSS8)
    private String status;          // 상태 (REJECTED, APPROVED...)
    private String comment;         // 코멘트 내용
    private LocalDateTime createdAt; // 작성일

    public ManagerCommentResponseDto(IpProposalComment entity) {
        this.commentId = entity.getId();
        this.authorName = entity.getAuthorName();
        this.authorId = entity.getAuthorId();
        this.status = entity.getStatus().name(); // 또는 getDescription() 사용 가능
        this.comment = entity.getComment();
        this.createdAt = entity.getCreatedAt();
    }
}