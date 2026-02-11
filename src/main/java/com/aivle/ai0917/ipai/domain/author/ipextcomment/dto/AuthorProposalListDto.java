package com.aivle.ai0917.ipai.domain.author.ipextcomment.dto;

import com.aivle.ai0917.ipai.domain.manager.ipext.model.IpProposal;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AuthorProposalListDto {
    private Long id;
    private String title;
    private String managerId; // 담당 매니저
    private String status;    // 제안서 상태
    private String targetFormat; // 타겟 포맷 (웹툰, 드라마 등)
    private LocalDateTime createdAt;

    public AuthorProposalListDto(IpProposal entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.managerId = entity.getManagerId();
        this.status = entity.getStatus().getDescription(); // 한글 설명
        this.targetFormat = entity.getTargetFormat() != null ? entity.getTargetFormat().getDescription() : "";
        this.createdAt = entity.getCreatedAt();
    }
}