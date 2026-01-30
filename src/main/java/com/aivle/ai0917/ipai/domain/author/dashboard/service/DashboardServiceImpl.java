package com.aivle.ai0917.ipai.domain.author.dashboard.service;

import com.aivle.ai0917.ipai.domain.author.dashboard.dto.DashboardSummaryResponseDto;
import com.aivle.ai0917.ipai.domain.author.dashboard.repository.AuthorDashboardStatsRepository;
import com.aivle.ai0917.ipai.domain.notice.dto.NoticeResponseDto;
import com.aivle.ai0917.ipai.domain.notice.service.NoticeService;
import jakarta.persistence.EntityManager;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//import com.aivle.ai0917.ipai.domain.author.works.model.Work;
//import com.aivle.ai0917.ipai.domain.author.works.model.WorkStatus;
//import com.aivle.ai0917.ipai.domain.author.works.repository.WorkRepository;
import com.aivle.ai0917.ipai.domain.user.model.User;
import com.aivle.ai0917.ipai.domain.user.repository.UserRepository;

import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final AuthorDashboardStatsRepository statsRepository; // 뷰 리포지토리 사용
    private final NoticeService noticeService;
    private final EntityManager entityManager;
//
//    private final WorkRepository workRepository;
    private final UserRepository userRepository;

        // works 지워서 동작 안함
        @Override
        public DashboardSummaryResponseDto getDashboardSummary(String integrationId) {
            // author_id(users.id)로 조회하여 이름 중복 문제를 해결합니다.
            String sql = """
        SELECT
            COUNT(*) FILTER (WHERE w.status = 'ONGOING')   AS ongoing_count,
            COUNT(*) FILTER (WHERE w.status = 'COMPLETED') AS completed_count,
            COUNT(DISTINCT s.id)                           AS setting_book_count
        FROM works w
        JOIN users u
            ON u.integration_id = w.user_integration_id
        LEFT JOIN setting s
            ON u.integration_id = ANY (s.userid)
        WHERE u.integration_id = :integrationId
        """;

            Object[] result = (Object[]) entityManager.createNativeQuery(sql)
                    .setParameter("integrationId", integrationId)
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


//    // 이부분은 작품 등록할 때 추가를 하는 것으로 결정 [move]
//
//    /**
//     *
//     * @param title
//     * @param description
//     */
//    @Transactional
//    public void saveNewWork(String title, String description) {
//        // 1. 현재 로그인한 유저의 내부 PK(Long) 가져오기 (SecurityContext 기반)
//        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//
//        // 2. 해당 유저의 엔티티 조회
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
//
//        // 3. 작품 엔티티 생성 시 유저의 integrationId를 자동으로 채움
//        Work work = Work.builder()
//                .title(title)
//                .description(description)
//                .userIntegrationId(user.getIntegrationId()) // ✅ 8자리 고유값 자동 할당
//                .writer(user.getName()) // 유저 이름 저장
//                .status(WorkStatus.ONGOING)
//                .build();
//
//        workRepository.save(work);
//    }
}