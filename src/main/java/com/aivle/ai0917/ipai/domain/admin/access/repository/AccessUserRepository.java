package com.aivle.ai0917.ipai.domain.admin.access.repository;

import com.aivle.ai0917.ipai.domain.admin.access.model.UserRole;
import com.aivle.ai0917.ipai.domain.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccessUserRepository extends JpaRepository<User, Long> {

    // 역할별 카운트 (요약 정보용)
    long countByRole(UserRole role);

    @Query("SELECT u FROM User u WHERE " +
            "(:keyword IS NULL OR u.name LIKE %:keyword% OR u.email LIKE %:keyword%) " +
            "AND (:role IS NULL OR u.role = :role)")
    Page<User> searchUsers(@Param("keyword") String keyword,
                           @Param("role") UserRole role,
                           Pageable pageable);

    // 검색 및 필터링 (동적 쿼리가 필요할 경우 QueryDSL 추천, 여기선 JPA 기본 메서드)
    Page<User> findByNameContainingOrEmailContainingAndRole(
            String name, String email, UserRole role, Pageable pageable
    );

    Page<User> findByNameContainingOrEmailContaining(
            String name, String email, Pageable pageable
    );
}