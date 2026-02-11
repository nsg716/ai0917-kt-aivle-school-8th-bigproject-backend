package com.aivle.ai0917.ipai.domain.author.ipextcomment.dto;

import com.aivle.ai0917.ipai.domain.manager.ipext.model.IpProposal;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AuthorProposalDetailDto {
    private Long id;
    private String title;
    private String summary;   // 주요 기획 의도 (expMarket 등 활용)
    private String targetGenre;
    private String worldSetting;
    private String filePath;  // PDF 다운로드 경로 또는 URL

    // 작가가 남긴 코멘트 정보 (필요시 리스트로 확장 가능)
    private List<AuthorCommentDto> myComments;

    private AuthorCommentDto myComment;

    public AuthorProposalDetailDto(IpProposal entity, AuthorCommentDto myComment) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.summary = entity.getExpCreative();
        this.targetGenre = entity.getTargetGenre();
        this.worldSetting = entity.getWorldSetting() != null ? entity.getWorldSetting().getDescription() : "";
        this.filePath = entity.getFilePath();
        this.myComment = myComment;
    }
}