package com.aivle.ai0917.ipai.domain.admin.info.controller;

import com.aivle.ai0917.ipai.domain.admin.info.dto.UnifiedAdminNoticeDto;
import com.aivle.ai0917.ipai.domain.admin.info.service.AdminNoticeAggregationService;
import com.aivle.ai0917.ipai.domain.admin.info.service.AdminNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자 알림 통합 API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/sysnotice")
@RequiredArgsConstructor
public class AdminController {

    private final AdminNoticeService adminNoticeService;
    private final AdminNoticeAggregationService aggregationService;



    /**
     * [GET] /api/v1/admin/sysnotice
     * 읽지 않은 알림만 조회하도록 변경
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAdminNotices(
            @RequestParam(defaultValue = "false") boolean all) {

        log.info("Fetching admin notices (all={})", all);

        List<UnifiedAdminNoticeDto> notices;
        if (all) {
            notices = aggregationService.getAllAdminNotices();
        } else {
            // 기본적으로 읽지 않은 알림만 반환
            notices = aggregationService.getUnreadAdminNotices();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("notices", notices);
        response.put("totalCount", notices.size());
        // 읽지 않은 알림만 조회했을 경우 unreadCount는 전체 사이즈와 동일
        response.put("unreadCount", notices.stream().filter(n -> !n.isRead()).count());

        return ResponseEntity.ok(response);
    }

    /**
     * [GET] /api/v1/admin/sysnotice/subscribe
     *
     * 실시간 알림 구독 (SSE)
     * - 시스템 메트릭 임계치 초과 알림 (5분마다 체크)
     * - 시스템 로그 알림 (ERROR/WARNING 발생 시)
     * - 배포 정보 알림 (배포 발생 시)
     * - 관리자 커스텀 알림
     *
     * @param adminId 관리자 ID
     * @return SSE Emitter
     */
    @GetMapping(value = "/subscribe", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter subscribe(@RequestParam Long adminId, jakarta.servlet.http.HttpServletResponse response)  {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/event-stream;charset=UTF-8");

        log.info("Admin {} subscribed to real-time notifications", adminId);
        return adminNoticeService.subscribe(adminId);
    }



    /**
     * [PATCH] /api/v1/admin/sysnotice/{source}/{id}/read
     *
     * 알림 읽음 처리
     *
     * @param source 알림 소스 (SYSTEM_METRIC, SYSTEM_LOG, DEPLOYMENT, ADMIN_CUSTOM)
     * @param id 알림 ID
     * @return 처리 결과
     */
    @PatchMapping("/{source}/{id}/read")
    public ResponseEntity<Map<String, String>> markAsRead(
            @PathVariable String source,
            @PathVariable Long id) {

        log.info("Marking notification as read: source={}, id={}", source, id);

        adminNoticeService.markAsRead(source, id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "알림이 읽음 처리되었습니다.");
        response.put("source", source);
        response.put("id", id.toString());

        return ResponseEntity.ok(response);
    }

    /**
     * [GET] /api/v1/admin/sysnotice/stats
     *
     * 알림 통계 조회
     *
     * @param hoursBack 조회 시간 범위 (기본 24시간)
     * @return 알림 통계
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getNotificationStats(
            @RequestParam(defaultValue = "24") int hoursBack) {

        log.info("Fetching notification stats: hoursBack={}", hoursBack);

        List<UnifiedAdminNoticeDto> notices =
                aggregationService.getUnifiedNotices(hoursBack, 1000);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", notices.size());
        stats.put("unread", notices.stream().filter(n -> !n.isRead()).count());

        // 소스별 카운트
        Map<String, Long> bySource = new HashMap<>();
        for (UnifiedAdminNoticeDto.NoticeSource source : UnifiedAdminNoticeDto.NoticeSource.values()) {
            long count = notices.stream()
                    .filter(n -> n.getSource() == source)
                    .count();
            bySource.put(source.name(), count);
        }
        stats.put("bySource", bySource);

        // 심각도별 카운트
        Map<String, Long> bySeverity = new HashMap<>();
        for (UnifiedAdminNoticeDto.NoticeSeverity severity : UnifiedAdminNoticeDto.NoticeSeverity.values()) {
            long count = notices.stream()
                    .filter(n -> n.getSeverity() == severity)
                    .count();
            bySeverity.put(severity.name(), count);
        }
        stats.put("bySeverity", bySeverity);

        return ResponseEntity.ok(stats);
    }

    /**
     * [PATCH] /api/v1/admin/sysnotice/read-all
     * 모든 알림을 한꺼번에 읽음 처리
     */
    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead() {
        log.info("Marking all notifications as read");

        adminNoticeService.markAllAsRead();

        Map<String, String> response = new HashMap<>();
        response.put("message", "모든 알림이 읽음 처리되었습니다.");

        return ResponseEntity.ok(response);
    }
}