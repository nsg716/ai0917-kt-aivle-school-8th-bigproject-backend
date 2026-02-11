package com.aivle.ai0917.ipai.domain.author.analyze.dto;

import com.aivle.ai0917.ipai.domain.author.episodes.model.ManuscriptView;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EpisodeBriefDto {
    private Long id;
    private String subtitle;

    @JsonProperty("ep_num")
    private Integer epNum;

    public static EpisodeBriefDto from(ManuscriptView view) {
        return EpisodeBriefDto.builder()
                .id(view.getId())
                .subtitle(view.getSubtitle())
                .epNum(view.getEpisode())
                .build();
    }
}