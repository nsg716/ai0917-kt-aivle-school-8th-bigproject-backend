package com.aivle.ai0917.ipai.domain.manager.iptrend.repository;

import com.aivle.ai0917.ipai.domain.manager.iptrend.model.IpTrendReport;
import com.aivle.ai0917.ipai.domain.manager.iptrend.model.IpTrendReport.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * IP 트렌드 리포트 Repository
 */
public interface IpTrendReportRepository extends JpaRepository<IpTrendReport, Long> {

    /**
     * 가장 최근 생성된 리포트 조회
     */
    Optional<IpTrendReport> findFirstByStatusOrderByCreatedAtDesc(ReportStatus status);

    /**
     * 상태별 리포트 목록 조회 (페이징)
     */
    Page<IpTrendReport> findByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);

    /**
     * 전체 리포트 목록 조회 (페이징)
     */
    Page<IpTrendReport> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 특정 기간 내 생성된 리포트 조회
     */
    List<IpTrendReport> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * 특정 날짜의 리포트 존재 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM IpTrendReport r " +
            "WHERE r.status = :status " +
            "AND DATE(r.analysisDate) = DATE(:analysisDate)")
    boolean existsByStatusAndAnalysisDate(
            @Param("status") ReportStatus status,
            @Param("analysisDate") LocalDateTime analysisDate
    );

    /**
     * 파일명으로 리포트 조회
     */
    Optional<IpTrendReport> findByFileName(String fileName);

    /**
     * 상태별 리포트 개수 조회
     */
    long countByStatus(ReportStatus status);
}