package com.aivle.ai0917.ipai.domain.author.episodes.dto;

import com.aivle.ai0917.ipai.domain.author.episodes.model.ManuscriptView;
import lombok.Getter;

// ManuscriptResponseDto.java (수정)

@Getter
public class ManuscriptResponseDto {
    private Long id;
    private String userId;
    private Long workId;
    private String title;
    private Integer episode;
    private String subtitle;
    private String txt;

    public ManuscriptResponseDto(ManuscriptView manuscript, String txt) {
        this.id = manuscript.getId();
        this.userId = manuscript.getUserId();
        this.workId = manuscript.getWorkId();
        this.title = manuscript.getTitle();
        this.episode = manuscript.getEpisode();
        this.subtitle = manuscript.getSubtitle();
        this.txt = txt;
    }

    public ManuscriptResponseDto(ManuscriptView manuscript) {
        this(manuscript, null);
    }
}
