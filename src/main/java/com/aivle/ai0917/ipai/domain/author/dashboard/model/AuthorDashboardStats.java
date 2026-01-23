package com.aivle.ai0917.ipai.domain.author.dashboard.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "author_dashboard_stats")
@Immutable // 뷰이므로 읽기 전용
@Getter
public class AuthorDashboardStats {
    @Id
    private String authorId;
    private long ongoingCount;
    private long settingBookCount;
    private long completedCount;
}