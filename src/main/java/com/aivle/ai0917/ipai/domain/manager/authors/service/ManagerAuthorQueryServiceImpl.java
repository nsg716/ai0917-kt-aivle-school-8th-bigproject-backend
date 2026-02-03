package com.aivle.ai0917.ipai.domain.manager.authors.service;

import com.aivle.ai0917.ipai.domain.admin.access.model.UserRole;
import com.aivle.ai0917.ipai.domain.author.works.model.WorkStatus;
import com.aivle.ai0917.ipai.domain.author.works.repository.WorkRepository;
import com.aivle.ai0917.ipai.domain.manager.authors.dto.*;
import com.aivle.ai0917.ipai.domain.manager.authors.repository.ManagerAuthorRepository;
import com.aivle.ai0917.ipai.domain.user.model.User;
import com.aivle.ai0917.ipai.domain.user.repository.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class ManagerAuthorQueryServiceImpl implements ManagerAuthorQueryService {

//    private static final String DELETED_STATUS = "DELETED"; // 프로젝트 상태값 정책에 맞게 유지/변경

    private final UserRepository userRepository;
    private final ManagerAuthorRepository managerAuthorRepository;
    private final WorkRepository workRepository;

    public ManagerAuthorQueryServiceImpl(UserRepository userRepository,
                                         ManagerAuthorRepository managerAuthorRepository,
                                         WorkRepository workRepository) {
        this.userRepository = userRepository;
        this.managerAuthorRepository = managerAuthorRepository;
        this.workRepository = workRepository;
    }

    @Override
    public AuthorSummaryResponseDto getSummary(Long managerUserId) {

        User manager = userRepository.findById(managerUserId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (manager.getRole() != UserRole.Manager) {
            throw new RuntimeException("매니저(Manager)만 조회할 수 있습니다.");
        }

        String mgrIntegrationId = manager.getIntegrationId();
        LocalDateTime now = LocalDateTime.now();

        long total = managerAuthorRepository.countByRoleAndManagerIntegrationId(UserRole.Author, mgrIntegrationId);

        long newAuthors = managerAuthorRepository.countByRoleAndManagerIntegrationIdAndCreatedAtGreaterThanEqual(
                UserRole.Author, mgrIntegrationId, now.minusDays(7)
        );

        long activeAuthors = managerAuthorRepository.countByRoleAndManagerIntegrationIdAndLastActivityAtGreaterThanEqual(
                UserRole.Author, mgrIntegrationId, now.minusHours(1)
        );

        return AuthorSummaryResponseDto.builder()
                .totalAuthors(total)
                .newAuthors(newAuthors)
                .activeAuthors(activeAuthors)
                .build();
    }

    @Override
    public Page<AuthorCardResponseDto> getAuthors(Long managerUserId, String keyword, Pageable pageable) {

        User manager = userRepository.findById(managerUserId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (manager.getRole() != UserRole.Manager) {
            throw new RuntimeException("매니저(Manager)만 조회할 수 있습니다.");
        }

        String mgrIntegrationId = manager.getIntegrationId();
        String searchKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        boolean sortByWorkCount = pageable.getSort().stream()
                .anyMatch(o -> o.getProperty().equalsIgnoreCase("workCount"));

        Page<User> userPage;

        if (sortByWorkCount) {
            Sort.Order order = pageable.getSort().getOrderFor("workCount");
            boolean asc = (order != null && order.getDirection().isAscending());

            Pageable onlyPage = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

            userPage = asc
                    ? managerAuthorRepository.findMatchedAuthorsOrderByWorkCountAsc(
                    mgrIntegrationId, searchKeyword, UserRole.Author, onlyPage
            )
                    : managerAuthorRepository.findMatchedAuthorsOrderByWorkCountDesc(
                    mgrIntegrationId, searchKeyword, UserRole.Author, onlyPage
            );

        } else {
            userPage = managerAuthorRepository.findMatchedAuthors(
                    mgrIntegrationId, searchKeyword, UserRole.Author, pageable
            );
        }

        LocalDateTime activeThreshold = LocalDateTime.now().minusHours(1);

        return userPage.map(u -> {

            // ✅ 변경: primaryAuthorId 기준 카운트 (+ DELETED 제외)
            long workCount = workRepository.countByPrimaryAuthorIdAndStatusNot(u.getIntegrationId(), WorkStatus.DELETED);

            String status = (u.getLastActivityAt() != null && u.getLastActivityAt().isAfter(activeThreshold))
                    ? "ACTIVE"
                    : "INACTIVE";

            return AuthorCardResponseDto.builder()
                    .id(u.getId())
                    .name(u.getName())
                    .email(u.getSiteEmail())
                    .workCount(workCount)
                    .createdAt(u.getCreatedAt())
                    .status(status)
                    .build();
        });
    }

    @Override
    public AuthorDetailResponseDto getAuthorDetail(Long managerUserId, Long authorId) {

        User manager = userRepository.findById(managerUserId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (manager.getRole() != UserRole.Manager) {
            throw new RuntimeException("매니저(Manager)만 조회할 수 있습니다.");
        }

        String mgrIntegrationId = manager.getIntegrationId();

        User author = managerAuthorRepository.findMatchedAuthorDetail(authorId, mgrIntegrationId, UserRole.Author)
                .orElseThrow(() -> new RuntimeException("작가를 찾을 수 없거나 권한이 없습니다."));

        // ✅ 변경: primaryAuthorId 기준 최근 5개 (+ DELETED 제외)
        var recentWorks = workRepository
                .findTop5ByPrimaryAuthorIdAndStatusNotOrderByCreatedAtDesc(author.getIntegrationId(), WorkStatus.DELETED)
                .stream()
                .map(w -> WorkSummaryDto.builder()
                        .id(w.getId())
                        .title(w.getTitle())
                        .createdAt(w.getCreatedAt())
                        .build())
                .toList();

        String lastLogin = (author.getLastActivityAt() == null) ? null : author.getLastActivityAt().toString();

        return AuthorDetailResponseDto.builder()
                .id(author.getId())
                .name(author.getName())
                .email(author.getSiteEmail())
                .gender(author.getGender())
                .birthYear(author.getBirthYear())
                .birthday(author.getBirthday())
                .createdAt(author.getCreatedAt())
                .lastLogin(lastLogin)
                .recentWorks(recentWorks)
                .build();
    }
}
