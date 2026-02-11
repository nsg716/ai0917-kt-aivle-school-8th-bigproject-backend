package com.aivle.ai0917.ipai.domain.manager.info.controller;

import com.aivle.ai0917.ipai.domain.manager.info.dto.ManagerNoticeDto;
import com.aivle.ai0917.ipai.domain.manager.info.service.ManagerNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/manager/sysnotice")
@RequiredArgsConstructor
public class ManagerNoticeController {

    private final ManagerNoticeService managerNoticeService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotices(
            @RequestParam String integrationId,
            @RequestParam(defaultValue = "false") boolean all) {

        List<ManagerNoticeDto> notices = managerNoticeService.getNotices(integrationId, !all);

        Map<String, Object> response = new HashMap<>();
        response.put("notices", notices);
        response.put("count", notices.size());

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestParam String integrationId) {
        return managerNoticeService.subscribe(integrationId);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @RequestParam String integrationId,
            @PathVariable Long id) {

        managerNoticeService.markAsRead(integrationId, id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@RequestParam String integrationId) {
        managerNoticeService.markAllAsRead(integrationId);
        return ResponseEntity.ok().build();
    }
}