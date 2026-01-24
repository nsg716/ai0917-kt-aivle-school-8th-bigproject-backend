// DTO - 수정 요청
package com.aivle.ai0917.ipai.domain.author.lorebook.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SettingBookUpdateRequestDto {
    private String title;
    private String keyword;
    private String subtitle;
    private String settings;
}
