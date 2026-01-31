package com.aivle.ai0917.ipai.domain.author.lorebook.dto;

import com.aivle.ai0917.ipai.domain.author.lorebook.model.SettingBookView;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;

import java.util.List;

@Getter
public class SettingBookResponseDto {

    private Long id;
    private String userId; // 프론트는 여전히 String으로 받음
    private Long workId;
    private String category;
    private String keyword;
    private JsonNode setting;
    private List<Integer> epNum;

    public SettingBookResponseDto(SettingBookView view) {
        this.id = view.getId();
        // [수정] List<String>에서 첫 번째 요소 추출 (또는 로직에 맞게 처리)
        this.userId = (view.getUserId() != null && !view.getUserId().isEmpty())
                ? view.getUserId().get(0)
                : null;
        this.workId = view.getWorkId();
        this.category = view.getCategory();
        this.keyword = view.getKeyword();
        this.setting = view.getSetting();
        this.epNum = view.getEpNum();
    }
}