package com.aivle.ai0917.ipai.domain.manager.ipext.model;

import jakarta.persistence.*;
import lombok.*;
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

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "writer_name", nullable = false)
    private String writerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "comment_text", columnDefinition = "TEXT", nullable = false)
    private String comment;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ========== [Inner Enum] 코멘트 상태 ==========
    @Getter
    @AllArgsConstructor
    public enum Status {
        PENDING("대기"), APPROVED("승인"), REJECTED("반려");
        private final String description;
    }
}