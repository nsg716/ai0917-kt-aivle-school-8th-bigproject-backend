package com.aivle.ai0917.ipai.domain.author.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardSummaryResponseDto {
    private long ongoingCount;      // 진행 중인 작품
    private long settingBookCount;  // 생성된 설정집
    private long completedCount;    // 완결 작품
}