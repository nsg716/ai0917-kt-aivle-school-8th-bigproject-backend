package com.aivle.ai0917.ipai.domain.author.ipextcomment.model;

import com.aivle.ai0917.ipai.domain.manager.ipext.model.IpProposal;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "ip_proposal_comments")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class IpProposalComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ip_proposal_id", nullable = false)
    private IpProposal ipProposal;

    // [수정 1] DB 컬럼명 변경 반영 (user_id -> author_id)
    @Column(name = "author_id", nullable = false)
    private String authorId;

    // [수정 2] DB 컬럼명 변경 반영 (writer_name -> author_name)
    @Column(name = "author_name", nullable = false)
    private String authorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "comment_text", columnDefinition = "TEXT", nullable = false)
    private String comment;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // [확인] 아까 VARCHAR(8)로 잘 변경하셨으므로 그대로 유지
    @Column(name = "manager_id", length = 8)
    private String managerId;

    @PrePersist
    public void onPrePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public void update(Status status, String comment) {
        if (status != null) {
            this.status = status;
        }
        if (comment != null) {
            this.comment = comment;
        }
    }
    // ========== [Inner Enum] 코멘트 상태 ==========
    public void archive() {
        this.status = Status.ARCHIVED;
    }

    // ========== [Inner Enum] 코멘트 상태 ==========
    @Getter
    @AllArgsConstructor
    public enum Status {
        PENDING("대기"),
        APPROVED("승인"),
        REJECTED("반려"),
        // [추가] 제안서 수정으로 인해 유효하지 않게 된 상태
        ARCHIVED("미사용");

        private final String description;
    }
}