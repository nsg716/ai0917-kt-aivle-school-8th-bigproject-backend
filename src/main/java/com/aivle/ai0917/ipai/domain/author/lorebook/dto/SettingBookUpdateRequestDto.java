package com.aivle.ai0917.ipai.domain.author.lorebook.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SettingBookUpdateRequestDto {

    // 설정집의 키워드 (예: "김독자", "스타스트림")
    private String keyword;

    // 설정 내용 (JSON 형식의 문자열)
    // DB의 'setting' 컬럼(jsonb 타입)에 저장됩니다.
    private String settings;
}