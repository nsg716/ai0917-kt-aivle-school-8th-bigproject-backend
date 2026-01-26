package com.aivle.ai0917.ipai.domain.author.dashboard.repository;

import com.aivle.ai0917.ipai.domain.author.dashboard.model.AuthorDashboardStatsView;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AuthorDashboardStatsRepository extends JpaRepository<AuthorDashboardStatsView, String> {
    Optional<AuthorDashboardStatsView> findByAuthorId(String authorId);
}