package com.aivle.ai0917.ipai.domain.author.dashboard.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "author_dashboard_stats") // 1단계에서 생성한 뷰 이름
@Getter
public class AuthorDashboardStatsView {

    @Id
    @Column(name = "author_integration_id") // 뷰의 컬럼명
    private String authorId;

    @Column(name = "ongoing_count")
    private long ongoingCount;

    @Column(name = "setting_book_count")
    private long settingBookCount;

    @Column(name = "completed_count")
    private long completedCount;
}