package com.aivle.ai0917.ipai.domain.manager.ipext.model;

import com.aivle.ai0917.ipai.domain.author.ipextcomment.model.IpProposalComment;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "manager_id", nullable = false, length = 8)
    private String managerId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "lorebook_ids", columnDefinition = "bigint[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Builder.Default
    private List<Long> lorebookIds = new ArrayList<>();

    // [통합] 별도 파일이었던 Enum을 내부로 이동
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.NEW;

    // ========== 사업 전략 (공통 Enum 사용) ==========

    @Enumerated(EnumType.STRING)
    @Column(name = "target_format", length = 50)
    private TargetFormat targetFormat;

    // 2. [수정] 장르 전략 (Enum 삭제 -> 텍스트만 저장)
    // UI의 "TARGET GENRE" 입력값 (예: 사이버펑크, 로맨스 판타지...)
    @Column(name = "target_genre", columnDefinition = "TEXT")
    private String targetGenre;

    @Column(name = "genre_strategy_text", columnDefinition = "TEXT")
    private String genreStrategyText;

    @Enumerated(EnumType.STRING)
    @Column(name = "world_setting", length = 50)
    private WorldSetting worldSetting;

    @Column(name = "target_ages", columnDefinition = "varchar[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Builder.Default
    private List<String> targetAges = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "target_gender", length = 20)
    private TargetGender targetGender;

    @Enumerated(EnumType.STRING)
    @Column(name = "budget_scale", length = 20)
    private BudgetScale budgetScale;

    @Column(name = "tone_and_manner", columnDefinition = "TEXT")
    private String toneAndManner;

    // ========== [핵심 변경] 매체 상세 설정 (JSONB 통합) ==========
    // 기존: Long mediaDetailId -> 변경: JsonNode detail
    // IpMediaDetails 클래스에 정의된 DTO 객체들을 JSON으로 통째로 저장합니다.
    @Column(name = "media_detail", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode mediaDetail;

    @Column(name = "add_prompt", columnDefinition = "TEXT")
    private String addPrompt;

    // ========== 요약 및 파일 정보 ==========
    // ========== 요약 및 전략 정보 (컬럼명 변경 반영) ==========
    @Column(name = "exp_market", columnDefinition = "TEXT")
    private String expMarket;

    @Column(name = "exp_creative", columnDefinition = "TEXT")
    private String expCreative;

    @Column(name = "exp_visual", columnDefinition = "TEXT")
    private String expVisual;

    @Column(name = "exp_world", columnDefinition = "TEXT")
    private String expWorld;

    @Column(name = "exp_business", columnDefinition = "TEXT")
    private String expBusiness;

    @Column(name = "exp_production", columnDefinition = "TEXT")
private String expProduction;

    @Column(name = "file_path", length = 500) private String filePath;
    @Column(name = "file_size") private Long fileSize;
    @Column(name = "original_filename", length = 255) private String originalFilename;


    @Column(name = "match_author_id", columnDefinition = "varchar[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Builder.Default
    private List<String> matchAuthorIds = new ArrayList<>();
    // ========== 코멘트 관계 ==========
    @OneToMany(mappedBy = "ipProposal", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<IpProposalComment> comments = new ArrayList<>();

    // ========== 시간 정보 ==========
    @CreatedDate @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @LastModifiedDate private LocalDateTime updatedAt;


    @PrePersist
    public void onPrePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // [추가] 업데이트 전 실행되는 메서드
    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    // =================================================================
    // 비즈니스 로직 메서드
    // =================================================================

    public void updateFromRequest(String title,
                                  TargetFormat targetFormat,
                                  String targetGenre,
                                  WorldSetting worldSetting,
                                  List<String> targetAges,
                                  TargetGender targetGender,
                                  BudgetScale budgetScale,
                                  String toneAndManner,
                                  JsonNode mediaDetail,
                                  String addPrompt) {
        if (title != null) this.title = title;
        if (targetFormat != null) this.targetFormat = targetFormat;
        if (targetGenre != null) this.targetGenre = targetGenre;
        if (worldSetting != null) this.worldSetting = worldSetting;
        if (targetAges != null) this.targetAges = targetAges;
        if (targetGender != null) this.targetGender = targetGender;
        if (budgetScale != null) this.budgetScale = budgetScale;
        if (toneAndManner != null) this.toneAndManner = toneAndManner;
        if (mediaDetail != null) this.mediaDetail = mediaDetail;
        if (addPrompt != null) this.addPrompt = addPrompt;
    }

    /**
     * 소프트 삭제 (Status만 변경)
     */
    public void softDelete() {
        this.status = Status.DELETED;
    }


    // ========== [Inner Enums] 공통 Enum 정의 ==========

    @Getter @AllArgsConstructor
    public enum Status {
        NEW("신규"), PENDING_APPROVAL("승인대기"), APPROVED("승인"), REJECTED("반려"), DELETED("삭제");
        private final String description;
    }

    @Getter @AllArgsConstructor
    public enum TargetFormat {
        WEBTOON("웹툰"), DRAMA("드라마"), MOVIE("영화"), GAME("게임"), SPINOFF("스핀오프"), COMMERCIAL_IMAGE("상업 이미지");
        private final String description;
    }

    @Getter @AllArgsConstructor
    public enum WorldSetting {
        SHARED("공유 세계관"),
        PARALLEL("평행 세계");
        private final String description;
    }

    @Getter @AllArgsConstructor
    public enum TargetGender {
        MALE("남성"), FEMALE("여성"), ALL("전체");
        private final String description;
    }

    @Getter @AllArgsConstructor
    public enum BudgetScale {
        SMALL("저예산(인디/실험적)"),
        MEDIUM("중예산(일반 상업)"),
        LARGE("고예산(블록버스터)"),
        BLOCKBUSTER("초대형(글로벌 타겟)");
        private final String description;
    }
}