package com.aivle.ai0917.ipai.domain.user.repository;

import com.aivle.ai0917.ipai.domain.admin.access.model.UserRole;
import com.aivle.ai0917.ipai.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * DB에서 사용자 조회/저장 담당
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNaverId(String naverId);

    // 고유 식별자로 사용자 조회
    Optional<User> findByIntegrationId(String integrationId);
    //이메일 로그인용
    Optional<User> findBySiteEmail(String siteEmail);

    boolean existsBySiteEmail(String siteEmail);

    Optional<User> findByNameAndSiteEmail(String name, String siteEmail);


    List<User> findAllByRole(UserRole role);

    // 전체 사용자 수
    @Query("SELECT COUNT(u) FROM User u")
    Long countTotalUsers();

    // 활성 세션 수 (예: 최근 30분 이내 활동)
    @Query("SELECT COUNT(DISTINCT u) FROM User u WHERE u.lastActivityAt >= :threshold")
    Integer countActiveSessions(@Param("threshold") LocalDateTime threshold);

    // 특정 기간 동안의 활성 사용자 수 (DAU 계산용)
    @Query("SELECT COUNT(DISTINCT u) FROM User u WHERE u.lastActivityAt BETWEEN :start AND :end")
    Integer countActiveUsersBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * @PreUpdate를 호출하지 않고 lastActivityAt만 업데이트
     * @param id
     * @param now
     * @return
     */
    @Modifying(clearAutomatically = true) // 실행 후 영속성 컨텍스트를 비워줌
    @Query("UPDATE User u SET u.lastActivityAt = :now WHERE u.id = :id")
    int updateLastActivity(@Param("id") Long id, @Param("now") LocalDateTime now);

    /**
     * Deactivated 상태이면서 업데이트된 지 7일이 지난 사용자 삭제
     *
     */
    @Modifying
    @Query("DELETE FROM User u WHERE u.role = :role AND u.lastActivityAt <= :threshold")
    int deleteExpiredDeactivatedUsers(@Param("role") UserRole role, @Param("threshold") LocalDateTime threshold);

    boolean existsByIntegrationId(String integrationId);
}