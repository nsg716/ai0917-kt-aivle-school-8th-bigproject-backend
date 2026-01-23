package com.aivle.ai0917.ipai.domain.author.dashboard.repository;

import com.aivle.ai0917.ipai.domain.author.dashboard.model.Work;
import com.aivle.ai0917.ipai.domain.author.dashboard.model.WorkStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkRepository extends JpaRepository<Work, Long> {
    long countByStatus(WorkStatus status);
}