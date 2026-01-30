package com.aivle.ai0917.ipai.domain.manager.iptrend.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * IP 트렌드 분석 PDF 리포트 엔티티
 *
 * Python 스크립트 실행으로 생성된 PDF 보고서 메타데이터를 저장
 */
@Entity
@Table(name = "ip_trend_report")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class IpTrendReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * PDF 파일명 (예: webnovel_trend_report_20250129.pdf)
     */
    @Column(nullable = false, length = 255)
    private String fileName;

    /**
     * 파일 저장 경로 (상대 경로 또는 절대 경로)
     */
    @Column(nullable = false, length = 500)
    private String filePath;

    /**
     * 파일 크기 (bytes)
     */
    @Column(nullable = false)
    private Long fileSize;

    /**
     * 분석 기준 날짜 (보고서가 분석한 데이터의 기준일)
     */
    @Column(nullable = false)
    private LocalDateTime analysisDate;

    /**
     * 파일 생성일시 (자동 생성)
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 데이터 출처 (예: Google Trends)
     */
    @Column(length = 100)
    private String dataSource;

    /**
     * 보고서 상태 (PENDING, COMPLETED, FAILED)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    /**
     * 비고 (에러 메시지 등)
     */
    @Column(columnDefinition = "TEXT")
    private String remarks;

    /**
     * 보고서 상태 Enum
     */
    public enum ReportStatus {
        PENDING,    // 생성 대기
        COMPLETED,  // 생성 완료
        FAILED      // 생성 실패
    }

    /**
     * 상태 업데이트 메서드
     */
    public void updateStatus(ReportStatus status, String remarks) {
        this.status = status;
        this.remarks = remarks;
    }

    /**
     * 파일 정보 업데이트 메서드
     */
    public void updateFileInfo(String filePath, Long fileSize) {
        this.filePath = filePath;
        this.fileSize = fileSize;
    }
}