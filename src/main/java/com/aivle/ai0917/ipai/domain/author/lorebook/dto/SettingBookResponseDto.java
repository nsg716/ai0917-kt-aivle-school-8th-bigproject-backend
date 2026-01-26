// 6. DTO 생성자 오버로드
package com.aivle.ai0917.ipai.domain.author.lorebook.dto;

import com.aivle.ai0917.ipai.domain.author.lorebook.model.SettingBookView;
import lombok.Getter;

import java.util.UUID;

@Getter
public class SettingBookResponseDto {

    private UUID id;
    private String[] userId;
    private String title;
    private String tag;
    private String keyword;
    private String settings;

    public SettingBookResponseDto(SettingBookView view) {
        this.id = view.getId();
        this.userId = view.getUserid();
        this.title = view.getTitle();
        this.tag = view.getTag();
        this.keyword = view.getKeyword();
        this.settings = view.getSettings();
    }
}
