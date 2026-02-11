package com.aivle.ai0917.ipai.domain.admin.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


// 대시보드 요약 (상단 4개 카드)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponseDto {
    private ServerStatusDto serverStatus;
    private Long totalUsers;
    private Long savedArtworks;
    private Long savedLorebooks;
    private Integer activeSessions;
}

