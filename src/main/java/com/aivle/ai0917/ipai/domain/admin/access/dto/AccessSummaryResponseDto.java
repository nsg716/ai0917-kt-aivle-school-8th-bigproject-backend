package com.aivle.ai0917.ipai.domain.admin.access.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


// 권한별 요약 정보 응답 (상단 카드용)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessSummaryResponseDto {
    private long adminCount;
    private long managerCount;
    private long authorCount;
    private long deactivatedCount;
}