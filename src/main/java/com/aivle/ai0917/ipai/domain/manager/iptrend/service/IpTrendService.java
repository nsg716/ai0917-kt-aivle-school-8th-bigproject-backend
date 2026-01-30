package com.aivle.ai0917.ipai.domain.manager.iptrend.service;

import com.aivle.ai0917.ipai.domain.manager.iptrend.dto.IpTrendResponseDto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * IP 트렌드 분석 서비스 인터페이스
 */
public interface IpTrendService {

    /**
     * 대시보드 데이터 조회
     */
    DashboardResponse getDashboardData();

    /**
     * PDF 리포트 목록 조회 (페이징)
     */
    Page<ReportListResponse> getReportList(Pageable pageable);

    /**
     * 특정 PDF 리포트 프리뷰 조회
     */
    ReportPreviewResponse getReportPreview(Long reportId);

    /**
     * 가장 최근 PDF 다운로드
     */
    byte[] downloadLatestReport();

    /**
     * 특정 PDF 다운로드
     */
    byte[] downloadReport(Long reportId);

    /**
     * Python 스크립트 실행하여 새 리포트 생성
     */
    GenerateReportResponse generateNewReport(GenerateReportRequest request);

    /**
     * 오늘 날짜 리포트가 이미 존재하는지 확인
     */
    boolean isReportExistsToday();
}