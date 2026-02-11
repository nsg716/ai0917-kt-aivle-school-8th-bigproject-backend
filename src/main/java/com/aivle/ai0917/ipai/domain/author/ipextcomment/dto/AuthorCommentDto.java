package com.aivle.ai0917.ipai.domain.author.ipextcomment.dto;

import com.aivle.ai0917.ipai.domain.author.ipextcomment.model.IpProposalComment;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AuthorCommentDto {
    private Long id;
    private String status;
    private String comment;
    private LocalDateTime createdAt;

    public AuthorCommentDto(IpProposalComment entity) {
        this.id = entity.getId();
        this.status = entity.getStatus().getDescription();
        this.comment = entity.getComment();
        this.createdAt = entity.getCreatedAt();
    }
}