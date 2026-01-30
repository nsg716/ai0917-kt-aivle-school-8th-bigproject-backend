package com.aivle.ai0917.ipai.domain.manager.iptrend.dto;

import com.aivle.ai0917.ipai.domain.manager.iptrend.model.IpTrendReport;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IP 트렌드 분석 Response DTO
 */
public class IpTrendResponseDto {

    /**
     * PDF 리포트 목록 응답
     */
    @Getter
    @Builder
    public static class ReportListResponse {
        private Long id;
        private String fileName;
        private Long fileSize;
        private String fileSizeFormatted; // 예: "2.5 MB"
        private LocalDateTime analysisDate;
        private LocalDateTime createdAt;
        private String status;
        private String dataSource;

        public static ReportListResponse from(IpTrendReport report) {
            return ReportListResponse.builder()
                    .id(report.getId())
                    .fileName(report.getFileName())
                    .fileSize(report.getFileSize())
                    .fileSizeFormatted(formatFileSize(report.getFileSize()))
                    .analysisDate(report.getAnalysisDate())
                    .createdAt(report.getCreatedAt())
                    .status(report.getStatus().name())
                    .dataSource(report.getDataSource())
                    .build();
        }

        private static String formatFileSize(Long bytes) {
            if (bytes == null || bytes == 0) return "0 B";

            double kb = bytes / 1024.0;
            if (kb < 1024) return String.format("%.2f KB", kb);

            double mb = kb / 1024.0;
            if (mb < 1024) return String.format("%.2f MB", mb);

            double gb = mb / 1024.0;
            return String.format("%.2f GB", gb);
        }
    }

    /**
     * PDF 프리뷰 응답
     */
    @Getter
    @Builder
    public static class ReportPreviewResponse {
        private Long id;
        private String fileName;
        private String fileUrl;  // 다운로드 URL
        private Long fileSize;
        private LocalDateTime analysisDate;
        private LocalDateTime createdAt;
        private String status;
        private PreviewMetadata metadata;

        @Getter
        @Builder
        public static class PreviewMetadata {
            private int totalPages;
            private String thumbnailUrl;
            private List<String> keywords;
            private String summary;
        }
    }

    /**
     * 대시보드 통계 응답
     */
    @Getter
    @Builder
    public static class DashboardResponse {
        private ReportSummary latestReport;
        private ReportStatistics statistics;
        private List<RecentReport> recentReports;

        @Getter
        @Builder
        public static class ReportSummary {
            private String fileName;
            private LocalDateTime createdAt;
            private String status;
            private Long fileSize;
        }

        @Getter
        @Builder
        public static class ReportStatistics {
            private long totalReports;
            private long completedReports;
            private long failedReports;
            private LocalDateTime lastGeneratedAt;
        }

        @Getter
        @Builder
        public static class RecentReport {
            private Long id;
            private String fileName;
            private LocalDateTime createdAt;
            private String status;
        }
    }

    /**
     * 리포트 생성 요청
     */
    @Getter
    @Builder
    public static class GenerateReportRequest {
        private LocalDateTime analysisDate;
        private String dataSource;
        private boolean forceRegenerate; // 기존 리포트가 있어도 재생성
    }

    /**
     * 리포트 생성 응답
     */
    @Getter
    @Builder
    public static class GenerateReportResponse {
        private Long reportId;
        private String fileName;
        private String status;
        private String message;
    }

    /**
     * 에러 응답
     */
    @Getter
    @Builder
    public static class ErrorResponse {
        private String errorCode;
        private String message;
        private LocalDateTime timestamp;
    }
}