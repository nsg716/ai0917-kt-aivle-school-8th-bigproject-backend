package com.aivle.ai0917.ipai.domain.author.lorebook.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SettingBookCreateRequestDto {
    // DTO는 순수 데이터 전달용이므로 userId, workId는 Controller에서 Param으로 주입받는 것이 좋음
    // 하지만, AI 서버와 통신할 때 필요하다면 포함 가능. 여기서는 DB 저장용 필드 위주 구성.

    private String category; // tag 대신 category 사용
    private String keyword;
    private String subtitle;

    // DB 컬럼은 jsonb이지만 Java에서는 String으로 받아서 바로 저장 (DB 형변환 쿼리 사용)
    private String settings;

    // DB 컬럼 ep_num (array)
    private List<Integer> episode;
}