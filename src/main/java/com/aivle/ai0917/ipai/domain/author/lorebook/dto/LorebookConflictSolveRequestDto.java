package com.aivle.ai0917.ipai.domain.author.lorebook.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LorebookConflictSolveRequestDto {
    private Long universeId; // 필요 시
    private Object setting; // JSON 데이터
}