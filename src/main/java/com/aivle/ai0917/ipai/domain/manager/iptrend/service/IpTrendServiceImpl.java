package com.aivle.ai0917.ipai.domain.manager.iptrend.service;

import com.aivle.ai0917.ipai.domain.admin.access.model.UserRole;
import com.aivle.ai0917.ipai.domain.manager.info.dto.ManagerNoticeDto;
import com.aivle.ai0917.ipai.domain.manager.info.service.ManagerNoticeService;
import com.aivle.ai0917.ipai.domain.manager.iptrend.dto.IpTrendResponseDto.*;
import com.aivle.ai0917.ipai.domain.manager.iptrend.model.IpTrendReport;
import com.aivle.ai0917.ipai.domain.manager.iptrend.model.IpTrendReport.ReportStatus;
import com.aivle.ai0917.ipai.domain.manager.iptrend.repository.IpTrendReportRepository;
import com.aivle.ai0917.ipai.domain.user.model.User;
import com.aivle.ai0917.ipai.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * IP 트렌드 분석 서비스 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IpTrendServiceImpl implements IpTrendService {

    private final IpTrendReportRepository reportRepository;
    private final ManagerNoticeService managerNoticeService;
    private final UserRepository userRepository;

    @Value("${iptrend.python.script.path:webnovel_trend_analysis_and_report.py}")
    private String pythonScriptPath;

    @Value("${iptrend.report.storage.path:./reports}")
    private String reportStoragePath;

    @Value("${iptrend.python.executable:python3}")
    private String pythonExecutable;

    @Override
    public DashboardResponse getDashboardData() {
        log.info("대시보드 데이터 조회 시작");

        // 최신 리포트 조회
        IpTrendReport latestReport = reportRepository
                .findFirstByStatusOrderByCreatedAtDesc(ReportStatus.COMPLETED)
                .orElse(null);

        // 통계 조회
        long totalReports = reportRepository.count();
        long completedReports = reportRepository.countByStatus(ReportStatus.COMPLETED);
        long failedReports = reportRepository.countByStatus(ReportStatus.FAILED);

        // 최근 5개 리포트 조회
        List<DashboardResponse.RecentReport> recentReports = reportRepository
                .findByStatusOrderByCreatedAtDesc(
                        ReportStatus.COMPLETED,
                        org.springframework.data.domain.PageRequest.of(0, 5)
                )
                .getContent()
                .stream()
                .map(report -> DashboardResponse.RecentReport.builder()
                        .id(report.getId())
                        .fileName(report.getFileName())
                        .createdAt(report.getCreatedAt())
                        .status(report.getStatus().name())
                        .build())
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .latestReport(latestReport != null ? DashboardResponse.ReportSummary.builder()
                        .fileName(latestReport.getFileName())
                        .createdAt(latestReport.getCreatedAt())
                        .status(latestReport.getStatus().name())
                        .fileSize(latestReport.getFileSize())
                        .build() : null)
                .statistics(DashboardResponse.ReportStatistics.builder()
                        .totalReports(totalReports)
                        .completedReports(completedReports)
                        .failedReports(failedReports)
                        .lastGeneratedAt(latestReport != null ? latestReport.getCreatedAt() : null)
                        .build())
                .recentReports(recentReports)
                .build();
    }

    @Override
    public Page<ReportListResponse> getReportList(Pageable pageable) {
        log.info("완료된 리포트 목록 조회: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        // findAll 대신 상태값을 인자로 받는 레포지토리 메서드 사용
        return reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.COMPLETED, pageable)
                .map(ReportListResponse::from);
    }

    @Override
    public ReportPreviewResponse getReportPreview(Long reportId) {
        log.info("리포트 프리뷰 조회: reportId={}", reportId);

        IpTrendReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("리포트를 찾을 수 없습니다: " + reportId));

        return ReportPreviewResponse.builder()
                .id(report.getId())
                .fileName(report.getFileName())
                .filepath(report.getFilePath())
                .fileSize(report.getFileSize())
                .analysisDate(report.getAnalysisDate())
                .createdAt(report.getCreatedAt())
                .status(report.getStatus().name())
                .build();
    }

    @Override
    public byte[] downloadLatestReport() {
        log.info("최신 리포트 다운로드 요청");

        IpTrendReport latestReport = reportRepository
                .findFirstByStatusOrderByCreatedAtDesc(ReportStatus.COMPLETED)
                .orElseThrow(() -> new IllegalArgumentException("다운로드 가능한 리포트가 없습니다."));

        return downloadReport(latestReport.getId());
    }

    @Override
    public byte[] downloadReport(Long reportId) {
        log.info("리포트 다운로드: reportId={}", reportId);

        IpTrendReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("리포트를 찾을 수 없습니다: " + reportId));

        if (report.getStatus() != ReportStatus.COMPLETED) {
            throw new IllegalStateException("완료되지 않은 리포트는 다운로드할 수 없습니다.");
        }

        try {
            Path filePath = Paths.get(report.getFilePath());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("파일 읽기 실패: {}", report.getFilePath(), e);
            throw new RuntimeException("파일을 읽을 수 없습니다.", e);
        }
    }

    @Override
    @Transactional
    public GenerateReportResponse generateNewReport(GenerateReportRequest request) {
        log.info("===============================================");
        log.info("새 리포트 생성 시작");
        log.info("===============================================");

        LocalDateTime analysisDate = request.getAnalysisDate() != null
                ? request.getAnalysisDate()
                : LocalDateTime.now();

        log.info("분석 기준 날짜: {}", analysisDate);
        log.info("데이터 출처: {}", request.getDataSource());
        log.info("강제 재생성: {}", request.isForceRegenerate());

        // 오늘 날짜 리포트가 이미 존재하는지 확인
        if (!request.isForceRegenerate() && isReportExistsToday()) {
            log.warn("⚠️ 오늘 날짜의 리포트가 이미 존재합니다.");
            throw new IllegalStateException("오늘 날짜의 리포트가 이미 존재합니다. 강제 재생성 옵션을 사용하세요.");
        }

        // 리포트 엔티티 생성 (PENDING 상태)
        String fileName = generateFileName(analysisDate);
        IpTrendReport report = IpTrendReport.builder()
                .fileName(fileName)
                .filePath("") // Python 실행 후 업데이트
                .fileSize(0L)
                .analysisDate(analysisDate)
                .createdAt(LocalDateTime.now())
                .dataSource(request.getDataSource() != null ? request.getDataSource() : "Google Trends")
                .status(ReportStatus.PENDING)
                .build();

        report = reportRepository.save(report);
        log.info("✅ 리포트 레코드 생성 완료");
        log.info("   - Report ID: {}", report.getId());
        log.info("   - File Name: {}", fileName);

        // Python 스크립트 비동기 실행
        final Long reportId = report.getId();
        executePythonScriptAsync(reportId);

        log.info("🚀 Python 스크립트 비동기 실행 시작");
        log.info("===============================================");

        return GenerateReportResponse.builder()
                .reportId(reportId)
                .fileName(fileName)
                .status("PENDING")
                .message("리포트 생성이 시작되었습니다. 완료까지 5-10분 소요될 수 있습니다.")
                .build();
    }

    @Override
    public boolean isReportExistsToday() {
        return reportRepository.existsByStatusAndAnalysisDate(
                ReportStatus.COMPLETED,
                LocalDateTime.now()
        );
    }

    /**
     * Python 스크립트 비동기 실행
     */
    private void executePythonScriptAsync(Long reportId) {
        new Thread(() -> {
            try {
                log.info("===============================================");
                log.info("Python 스크립트 실행 시작");
                log.info("Report ID: {}", reportId);
                log.info("===============================================");

                // 작업 디렉토리 설정
                File workingDir = new File(reportStoragePath);
                if (!workingDir.exists()) {
                    log.info("리포트 저장 디렉토리 생성: {}", workingDir.getAbsolutePath());
                    boolean created = workingDir.mkdirs();
                    if (!created) {
                        throw new IOException("디렉토리 생성 실패: " + workingDir.getAbsolutePath());
                    }
                }

                // Python 스크립트 파일 확인
                File scriptFile = new File(pythonScriptPath);
                if (!scriptFile.exists()) {
                    throw new FileNotFoundException("Python 스크립트 파일을 찾을 수 없습니다: " + pythonScriptPath);
                }

                log.info("Python 실행 파일: {}", pythonExecutable);
                log.info("스크립트 경로: {}", scriptFile.getAbsolutePath());
                log.info("작업 디렉토리: {}", workingDir.getAbsolutePath());

                // Python 스크립트 실행
                ProcessBuilder processBuilder = new ProcessBuilder(
                        pythonExecutable,
                        scriptFile.getAbsolutePath()
                );

                processBuilder.directory(workingDir);
                processBuilder.redirectErrorStream(true);

                log.info("→ Python 프로세스 시작...");
                Process process = processBuilder.start();

                // 출력 로그 수집
                StringBuilder output = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                        log.info("[Python] {}", line);
                    }
                }

                int exitCode = process.waitFor();
                log.info("===============================================");
                log.info("Python 스크립트 실행 완료");
                log.info("Exit Code: {}", exitCode);
                log.info("===============================================");

                if (exitCode == 0) {
                    log.info("✅ 정상 종료 - 리포트 파일 확인 중...");
                    updateReportSuccess(reportId, workingDir);
                } else {
                    log.error("❌ 비정상 종료 - Exit Code: {}", exitCode);
                    log.error("Output:\n{}", output.toString());
                    updateReportFailure(reportId, "스크립트 실행 실패 (Exit Code: " + exitCode + ")");
                }

            } catch (FileNotFoundException e) {
                log.error("❌ 파일을 찾을 수 없음: reportId={}", reportId, e);
                updateReportFailure(reportId, "파일 없음: " + e.getMessage());

            } catch (IOException e) {
                log.error("❌ I/O 오류 발생: reportId={}", reportId, e);
                updateReportFailure(reportId, "I/O 오류: " + e.getMessage());

            } catch (InterruptedException e) {
                log.error("❌ 프로세스 인터럽트: reportId={}", reportId, e);
                Thread.currentThread().interrupt();
                updateReportFailure(reportId, "프로세스 중단: " + e.getMessage());

            } catch (Exception e) {
                log.error("❌ 예상치 못한 오류: reportId={}", reportId, e);
                updateReportFailure(reportId, "실행 오류: " + e.getMessage());
            }
        }, "IpTrend-Python-Worker-" + reportId).start();
    }

    /**
     * 리포트 성공 처리
     */
    @Transactional
    protected void updateReportSuccess(Long reportId, File workingDir) {
        log.info("===============================================");
        log.info("리포트 성공 처리 시작: reportId={}", reportId);
        log.info("===============================================");

        IpTrendReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("리포트를 찾을 수 없습니다: " + reportId));

        try {
            // 생성된 PDF 파일 찾기
            log.info("→ PDF 파일 검색 중: {}", workingDir.getAbsolutePath());

            File[] pdfFiles = workingDir.listFiles((dir, name) ->
                    name.endsWith(" IP 트랜드 분석 보고서.pdf"));

            if (pdfFiles == null || pdfFiles.length == 0) {
                throw new FileNotFoundException("생성된 PDF 파일을 찾을 수 없습니다: " + workingDir.getAbsolutePath());
            }

            log.info("→ 발견된 PDF 파일 수: {}", pdfFiles.length);

            // 가장 최근 파일 선택
            File latestPdf = pdfFiles[0];
            for (File pdf : pdfFiles) {
                log.info("   - {}: {} bytes, 최종 수정: {}",
                        pdf.getName(),
                        pdf.length(),
                        new java.util.Date(pdf.lastModified()));

                if (pdf.lastModified() > latestPdf.lastModified()) {
                    latestPdf = pdf;
                }
            }

            String filePath = latestPdf.getAbsolutePath();
            Long fileSize = latestPdf.length();

            log.info("✅ 선택된 PDF: {}", latestPdf.getName());
            log.info("   - 경로: {}", filePath);
            log.info("   - 크기: {} bytes ({} KB)", fileSize, fileSize / 1024);

            report.updateFileInfo(filePath, fileSize);
            report.updateStatus(ReportStatus.COMPLETED, "생성 완료");
            reportRepository.save(report);

            log.info("✅ 리포트 DB 업데이트 완료");
            log.info("===============================================");
            log.info("리포트 생성 성공: reportId={}", reportId);
            log.info("===============================================");


            List<User> managers = userRepository.findAllByRole(UserRole.Manager);

            for (User manager : managers) {
                managerNoticeService.sendNotice(
                        manager.getIntegrationId(), // String ID
                        ManagerNoticeDto.ManagerNoticeSource.IP_EXTREND,
                        "트렌드 리포트 생성 완료",
                        report.getFileName() + " 생성이 완료되었습니다.",
                        "/manager/iptrend/preview/" + reportId // 상세/프리뷰 페이지 이동
                );
            }
            log.info("모든 매니저({}명)에게 성공 알림 전송 완료", managers.size());
        } catch (FileNotFoundException e) {
            log.error("❌ PDF 파일을 찾을 수 없음: reportId={}", reportId, e);
            updateReportFailure(reportId, "파일 없음: " + e.getMessage());

        } catch (Exception e) {
            log.error("❌ 리포트 성공 처리 중 오류: reportId={}", reportId, e);
            updateReportFailure(reportId, "파일 처리 실패: " + e.getMessage());
        }
    }

    /**
     * 리포트 실패 처리
     */
    @Transactional
    protected void updateReportFailure(Long reportId, String errorMessage) {
        IpTrendReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("리포트를 찾을 수 없습니다."));

        report.updateStatus(ReportStatus.FAILED, errorMessage);
        reportRepository.save(report);

        log.error("리포트 생성 실패: reportId={}, error={}", reportId, errorMessage);

        List<User> managers = userRepository.findAllByRole(UserRole.Manager);

        for (User manager : managers) {
            managerNoticeService.sendNotice(
                    manager.getIntegrationId(), // String ID
                    ManagerNoticeDto.ManagerNoticeSource.IP_EXTREND,
                    "트렌드 리포트 생성 실패",
                    "오류가 발생했습니다: " + errorMessage,
                    "/manager/iptrend"
            );
        }
        log.info("모든 매니저({}명)에게 실패 알림 전송 완료", managers.size());
    }

    /**
     * 파일명 생성
     */
    private String generateFileName(LocalDateTime analysisDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월");
        return analysisDate.format(formatter) + " IP 트랜드 분석 보고서.pdf";
    }
}