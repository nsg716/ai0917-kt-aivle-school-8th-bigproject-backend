package com.aivle.ai0917.ipai.domain.author.manuscript.dto;

import com.aivle.ai0917.ipai.domain.author.manuscript.model.ManuscriptView;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ManuscriptResponseDto {
    private UUID id;
    private String userId;
    private String title;
    private Integer episode;
    private String subtitle;
    private String txt;

    public ManuscriptResponseDto(ManuscriptView manuscript) {
        this.id = manuscript.getId();
        this.userId = manuscript.getUserId();
        this.title = manuscript.getTitle();
        this.episode = manuscript.getEpisode();
        this.subtitle = manuscript.getSubtitle();
        this.txt = manuscript.getTxt();
    }
}