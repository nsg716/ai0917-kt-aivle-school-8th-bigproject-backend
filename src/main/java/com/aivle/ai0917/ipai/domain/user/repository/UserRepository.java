package com.aivle.ai0917.ipai.domain.user.repository;

import com.aivle.ai0917.ipai.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * DB에서 사용자 조회/저장 담당
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNaverId(String naverId);
    Optional<User> findBySiteEmail(String siteEmail);
    boolean existsBySiteEmail(String siteEmail);
}