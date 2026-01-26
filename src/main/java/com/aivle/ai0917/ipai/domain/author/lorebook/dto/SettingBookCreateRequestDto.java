// DTO - 생성 요청
package com.aivle.ai0917.ipai.domain.author.lorebook.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SettingBookCreateRequestDto {
    private String[] userId;
    private String keyword;
    private Integer[] episode;
    private String subtitle;
    private String settings;

    // 내부 세팅용
    private String title;
    private String tag;
}

