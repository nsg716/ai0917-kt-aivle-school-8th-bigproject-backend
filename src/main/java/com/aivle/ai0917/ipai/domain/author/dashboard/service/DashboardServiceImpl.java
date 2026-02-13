package com.aivle.ai0917.ipai.domain.author.dashboard.service;

import com.aivle.ai0917.ipai.domain.author.dashboard.dto.DashboardSummaryResponseDto;
import com.aivle.ai0917.ipai.domain.author.dashboard.repository.AuthorDashboardStatsRepository;
import com.aivle.ai0917.ipai.domain.notice.dto.NoticeResponseDto;
import com.aivle.ai0917.ipai.domain.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final AuthorDashboardStatsRepository statsRepository;
    private final NoticeService noticeService;

    @Override
    public DashboardSummaryResponseDto getDashboardSummary(String integrationId) {
        // DB View(author_dashboard_stats)를 통해 통계 데이터를 조회
        // 데이터가 없으면(신규 유저 등) 0으로 초기화된 객체 반환
        return statsRepository.findByAuthorId(integrationId)
                .map(stats -> DashboardSummaryResponseDto.builder()
                        .ongoingCount(stats.getOngoingCount())
                        .completedCount(stats.getCompletedCount())
                        .settingBookCount(stats.getSettingBookCount())
                        .build())
                .orElse(DashboardSummaryResponseDto.builder()
                        .ongoingCount(0L)
                        .completedCount(0L)
                        .settingBookCount(0L)
                        .build());
    }

    @Override
    public Page<NoticeResponseDto> getDashboardNotices(Pageable pageable) {
        return noticeService.getNoticeList(null, pageable);
    }
}