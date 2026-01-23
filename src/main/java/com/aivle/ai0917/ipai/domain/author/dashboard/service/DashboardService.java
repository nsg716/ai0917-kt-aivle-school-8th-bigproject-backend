package com.aivle.ai0917.ipai.domain.author.dashboard.service;

import com.aivle.ai0917.ipai.domain.author.dashboard.dto.DashboardSummaryResponseDto;
import com.aivle.ai0917.ipai.domain.notice.dto.NoticeResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DashboardService {

    DashboardSummaryResponseDto getDashboardSummary(String integrationId);

    Page<NoticeResponseDto> getDashboardNotices(Pageable pageable);
}