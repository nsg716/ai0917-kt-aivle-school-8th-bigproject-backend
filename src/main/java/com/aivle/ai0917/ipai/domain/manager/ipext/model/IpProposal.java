package com.aivle.ai0917.ipai.domain.manager.ipext.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * IP 기획안 엔티티
 * IP 매체 생성 및 사업 전략 설정을 관리하는 기획안 정보
 */
@Entity
@Table(name = "ip_proposal")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class IpProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 매니저 ID (통합 ID, 8자리)
     */
    @Column(name = "manager_id", nullable = false, length = 8)
    private String managerId;

    /**
     * 로어북 ID 목록 (복수)
     */
    @ElementCollection
    @CollectionTable(name = "ip_proposal_lorebooks", joinColumns = @JoinColumn(name = "ip_proposal_id"))
    @Column(name = "lorebook_id")
    @Builder.Default
    private List<Long> lorebookIds = new ArrayList<>();

    /**
     * 기획안 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private IpProposalStatus status = IpProposalStatus.NEW;

    /**
     * 생성일시
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정일시
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 삭제일시
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 기획안 제목
     */
    @Column(nullable = false, length = 255)
    private String title;

    // ========== 매체 생성 ==========

    /**
     * 타겟 포맷 (6개 값)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_format", length = 50)
    private TargetFormat targetFormat;

    /**
     * 장르 전략 (2개 값)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "genre_strategy", length = 50)
    private GenreStrategy genreStrategy;

    /**
     * 세계관 설정 (2개 값)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "world_setting", length = 50)
    private WorldSetting worldSetting;

    // ========== 사업 전략 설정 ==========

    /**
     * 타겟 연령대 (복수)
     */
    @ElementCollection
    @CollectionTable(name = "ip_proposal_target_ages", joinColumns = @JoinColumn(name = "ip_proposal_id"))
    @Column(name = "target_age")
    @Builder.Default
    private List<String> targetAges = new ArrayList<>();

    /**
     * 타겟 성별
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_gender", length = 20)
    private TargetGender targetGender;

    /**
     * 예산 규모 (3가지)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "budget_scale", length = 20)
    private BudgetScale budgetScale;

    /**
     * 톤 앤 매너
     */
    @Column(name = "tone_and_manner", columnDefinition = "TEXT")
    private String toneAndManner;

    // ========== 매체 상세 설정 ==========

    /**
     * 아트 스타일
     */
    @Column(name = "art_style", length = 100)
    private String artStyle;

    /**
     * 타겟 플랫폼
     */
    @Column(name = "target_platform", length = 100)
    private String targetPlatform;

    /**
     * 추가 프롬프트
     */
    @Column(name = "add_prompt", columnDefinition = "TEXT")
    private String addPrompt;

    // ========== 요약 6가지 ==========

    @Column(name = "summary_1", columnDefinition = "TEXT")
    private String summary1;

    @Column(name = "summary_2", columnDefinition = "TEXT")
    private String summary2;

    @Column(name = "summary_3", columnDefinition = "TEXT")
    private String summary3;

    @Column(name = "summary_4", columnDefinition = "TEXT")
    private String summary4;

    @Column(name = "summary_5", columnDefinition = "TEXT")
    private String summary5;

    @Column(name = "summary_6", columnDefinition = "TEXT")
    private String summary6;

    // ========== 코멘트 (복수) ==========

    @ElementCollection
    @CollectionTable(name = "ip_proposal_comments", joinColumns = @JoinColumn(name = "ip_proposal_id"))
    @Column(name = "comment", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> comments = new ArrayList<>();

    // ========== PDF 파일 정보 ==========

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    // ========== 비즈니스 메서드 ==========

    public void updateStatus(IpProposalStatus status) {
        this.status = status;
    }

    public void updateFileInfo(String filePath, Long fileSize, String originalFilename) {
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.originalFilename = originalFilename;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.status = IpProposalStatus.DELETED;
    }

    public void addLorebookId(Long lorebookId) {
        if (!this.lorebookIds.contains(lorebookId)) {
            this.lorebookIds.add(lorebookId);
        }
    }

    public void addComment(String comment) {
        this.comments.add(comment);
    }

    public void addTargetAge(String targetAge) {
        if (!this.targetAges.contains(targetAge)) {
            this.targetAges.add(targetAge);
        }
    }
}