package com.aivle.ai0917.ipai.domain.author.dashboard.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "author_dashboard_stats")
@org.hibernate.annotations.Subselect(
        "SELECT * FROM author_dashboard_stats"
)
@org.hibernate.annotations.Synchronize({})
@Getter
public class AuthorDashboardStats {
    @Id
    @Column(name = "author_integration_id")
    private String authorId;

    private long ongoingCount;
    private long settingBookCount;
    private long completedCount;
}