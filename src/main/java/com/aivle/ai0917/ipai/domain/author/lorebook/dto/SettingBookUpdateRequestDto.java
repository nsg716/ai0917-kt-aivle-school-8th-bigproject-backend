package com.aivle.ai0917.ipai.domain.author.lorebook.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SettingBookUpdateRequestDto {

    private String category; // [추가] AI 서버 전송용
    private String keyword;
    private String settings; // JSON String
    private List<Integer> episode; // [추가] AI 서버 전송용 (ep_num)
}