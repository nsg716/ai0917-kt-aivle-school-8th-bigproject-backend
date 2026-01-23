package com.aivle.ai0917.ipai.domain.author.dashboard.service;

import com.aivle.ai0917.ipai.domain.author.dashboard.dto.DashboardSummaryResponseDto;
import com.aivle.ai0917.ipai.domain.author.dashboard.repository.AuthorDashboardStatsRepository;
import com.aivle.ai0917.ipai.domain.notice.dto.NoticeResponseDto;
import com.aivle.ai0917.ipai.domain.notice.service.NoticeService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final AuthorDashboardStatsRepository statsRepository; // 뷰 리포지토리 사용
    private final NoticeService noticeService;
    private final EntityManager entityManager;

//    @Override
//    public DashboardSummaryResponseDto getDashboardSummary(String authorId) {
//        // 뷰 집계
//        // 나중에 수백만건 이상인 경우 Redis를 사용하는 것을 검토
//        // 뷰에서 해당 작가의 통계 한 줄을 가져옴
//        return statsRepository.findByAuthorId(authorId)
//                .map(stats -> DashboardSummaryResponseDto.builder()
//                        .ongoingCount(stats.getOngoingCount())
//                        .settingBookCount(stats.getSettingBookCount())
//                        .completedCount(stats.getCompletedCount())
//                        .build())
//                .orElseGet(() -> DashboardSummaryResponseDto.builder() // 데이터 없을 시 0 반환
//                        .ongoingCount(0)
//                        .settingBookCount(0)
//                        .completedCount(0)
//                        .build());
//    }
        @Override
        public DashboardSummaryResponseDto getDashboardSummary(Long userId) {
            // author_id(users.id)로 조회하여 이름 중복 문제를 해결합니다.
            String sql = """
                SELECT
                    COUNT(*) FILTER (WHERE w.status = 'ONGOING')  AS ongoing_count,
                    COUNT(*) FILTER (WHERE w.status = 'COMPLETED') AS completed_count,
                    COUNT(DISTINCT s.id)                          AS setting_book_count
                FROM works w
                LEFT JOIN users u ON u.id = w.user_id
                LEFT JOIN setting s ON u.name = ANY (s.writer)
                WHERE w.user_id = :userId
                """;

            Object[] result = (Object[]) entityManager.createNativeQuery(sql)
                    .setParameter("userId", userId)
                    .getSingleResult();

            return DashboardSummaryResponseDto.builder()
                    .ongoingCount(((Number) result[0]).longValue())
                    .completedCount(((Number) result[1]).longValue())
                    .settingBookCount(((Number) result[2]).longValue())
                    .build();
        }



    @Override
    public Page<NoticeResponseDto> getDashboardNotices(Pageable pageable) {
        return noticeService.getNoticeList(null, pageable);
    }
}