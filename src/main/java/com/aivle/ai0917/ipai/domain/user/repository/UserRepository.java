package com.aivle.ai0917.ipai.domain.user.repository;

import com.aivle.ai0917.ipai.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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



    // 전체 사용자 수
    @Query("SELECT COUNT(u) FROM User u")
    Long countTotalUsers();

    // 활성 세션 수 (예: 최근 30분 이내 활동)
    @Query("SELECT COUNT(DISTINCT u) FROM User u WHERE u.updatedAt >= :threshold")
    Integer countActiveSessions(@Param("threshold") LocalDateTime threshold);

    // 특정 기간 동안의 활성 사용자 수 (DAU 계산용)
    @Query("SELECT COUNT(DISTINCT u) FROM User u WHERE u.updatedAt BETWEEN :start AND :end")
    Integer countActiveUsersBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}