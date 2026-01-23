package com.aivle.ai0917.ipai.domain.author.dashboard.repository;

import com.aivle.ai0917.ipai.domain.author.dashboard.model.AuthorDashboardStats;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AuthorDashboardStatsRepository extends JpaRepository<AuthorDashboardStats, String> {
    Optional<AuthorDashboardStats> findByAuthorId(String authorId);
}