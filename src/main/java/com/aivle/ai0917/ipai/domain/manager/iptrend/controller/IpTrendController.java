package com.aivle.ai0917.ipai.domain.manager.iptrend.controller;

import com.aivle.ai0917.ipai.domain.manager.iptrend.dto.IpTrendResponseDto.*;
import com.aivle.ai0917.ipai.domain.manager.iptrend.scheduler.IpTrendReportScheduler;
import com.aivle.ai0917.ipai.domain.manager.iptrend.service.IpTrendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * IP 트렌드 분석 컨트롤러
 *
 * 웹소설 시장 트렌드 분석 PDF 리포트 관리 API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/manager/iptrend")
@RequiredArgsConstructor
public class IpTrendController {

    private final IpTrendService ipTrendService;
    private final IpTrendReportScheduler reportScheduler;

    /**
     * IP 트렌드 분석 대시보드 페이지
     *
     * GET /api/v1/manager/iptrend
     *
     * @return 대시보드 통계 및 최신 리포트 정보
     */
    @GetMapping
    public ResponseEntity<DashboardResponse> getIpTrendDashboard() {
        log.info("IP 트렌드 대시보드 조회 요청");

        DashboardResponse dashboard = ipTrendService.getDashboardData();
        return ResponseEntity.ok(dashboard);
    }

    /**
     * IP 트렌드 분석 PDF 리스트
     *
     * GET /api/v1/manager/iptrend/list
     *
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return PDF 리포트 목록 (페이징)
     */
    @GetMapping("/list")
    public ResponseEntity<Page<ReportListResponse>> getReportList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("리포트 목록 조회: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<ReportListResponse> reports = ipTrendService.getReportList(pageable);

        return ResponseEntity.ok(reports);
    }

    /**
     * IP 트렌드 분석 PDF 프리뷰
     *
     * GET /api/v1/manager/iptrend/preview/{reportId}
     *
     * @param reportId 리포트 ID
     * @return PDF 프리뷰 정보
     */
    @GetMapping("/preview/{reportId}")
    public ResponseEntity<ReportPreviewResponse> getReportPreview(
            @PathVariable Long reportId) {

        log.info("리포트 프리뷰 조회: reportId={}", reportId);

        ReportPreviewResponse preview = ipTrendService.getReportPreview(reportId);
        return ResponseEntity.ok(preview);
    }

    /**
     * 가장 최근 트렌드 분석 리포트 다운로드
     *
     * GET /api/v1/manager/iptrend/report
     *
     * @return PDF 파일 (바이너리)
     */
    @GetMapping("/report")
    public ResponseEntity<byte[]> downloadLatestReport() {
        log.info("최신 리포트 다운로드 요청");

        try {
            byte[] pdfContent = ipTrendService.downloadLatestReport();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "latest_trend_report.pdf");
            headers.setContentLength(pdfContent.length);

            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            log.warn("다운로드 가능한 리포트 없음: {}", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("리포트 다운로드 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 트렌드 분석 리포트 다운로드
     *
     * GET /api/v1/manager/iptrend/download/{reportId}
     *
     * @param reportId 리포트 ID
     * @return PDF 파일 (바이너리)
     */
    @GetMapping("/download/{reportId}")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long reportId) {
        log.info("리포트 다운로드 요청: reportId={}", reportId);

        try {
            byte[] pdfContent = ipTrendService.downloadReport(reportId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "trend_report_" + reportId + ".pdf");
            headers.setContentLength(pdfContent.length);

            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            log.warn("리포트를 찾을 수 없음: reportId={}", reportId);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.warn("완료되지 않은 리포트 다운로드 시도: reportId={}", reportId);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        } catch (Exception e) {
            log.error("리포트 다운로드 실패: reportId={}", reportId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 새 트렌드 분석 리포트 생성 요청
     *
     * POST /api/v1/manager/iptrend/generate
     *
     * @param request 생성 요청 정보 (선택 사항)
     * @return 생성 시작 응답
     */
    @PostMapping("/generate")
    public ResponseEntity<GenerateReportResponse> generateReport(
            @RequestBody(required = false) GenerateReportRequest request) {

        log.info("리포트 생성 요청");

        try {
            if (request == null) {
                request = GenerateReportRequest.builder()
                        .analysisDate(LocalDateTime.now())
                        .dataSource("Google Trends")
                        .forceRegenerate(false)
                        .build();
            }

            GenerateReportResponse response = ipTrendService.generateNewReport(request);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

        } catch (IllegalStateException e) {
            log.warn("리포트 생성 실패: {}", e.getMessage());

            ErrorResponse error = ErrorResponse.builder()
                    .errorCode("REPORT_ALREADY_EXISTS")
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(GenerateReportResponse.builder()
                            .status("FAILED")
                            .message(e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("리포트 생성 중 오류 발생", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenerateReportResponse.builder()
                            .status("ERROR")
                            .message("리포트 생성 중 오류가 발생했습니다.")
                            .build());
        }
    }

    /**
     * 오늘 날짜 리포트 존재 여부 확인
     *
     * GET /api/v1/manager/iptrend/exists-today
     *
     * @return 존재 여부
     */
    @GetMapping("/exists-today")
    public ResponseEntity<Boolean> checkReportExistsToday() {
        log.info("오늘 날짜 리포트 존재 여부 확인");

        boolean exists = ipTrendService.isReportExistsToday();
        return ResponseEntity.ok(exists);
    }

    /**
     * 스케줄러 수동 실행 (테스트/디버깅용)
     *
     * POST /api/v1/manager/iptrend/scheduler/run
     *
     * ⚠️ 주의: 이 API는 개발/테스트 목적으로만 사용하세요.
     * 운영 환경에서는 보안을 위해 제거하거나 접근 권한을 제한하세요.
     *
     * @return 실행 결과
     */
    @PostMapping("/scheduler/run")
    public ResponseEntity<Map<String, Object>> runSchedulerManually() {
        log.info("스케줄러 수동 실행 요청");

        try {
            reportScheduler.generateReportManually();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "스케줄러가 수동으로 실행되었습니다.");
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("스케줄러 수동 실행 중 오류 발생", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "스케줄러 실행 중 오류 발생: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}