package com.aivle.ai0917.ipai.domain.manager.dashboard.controller;

import com.aivle.ai0917.ipai.domain.manager.dashboard.dto.ManagerDashboardPageResponseDto;
import com.aivle.ai0917.ipai.domain.manager.dashboard.dto.ManagerDashboardSummaryResponseDto;
import com.aivle.ai0917.ipai.domain.manager.dashboard.service.ManagerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.aivle.ai0917.ipai.global.security.jwt.CurrentUserId;
@RestController
@RequestMapping("/api/v1/manager/dashboard")
@RequiredArgsConstructor
public class ManagerDashboardController {

    private final ManagerDashboardService managerDashboardService;

    /**
     * 운영자 대시보드 페이지
     */
    @GetMapping

    public ResponseEntity<ManagerDashboardPageResponseDto> getDashboard(@CurrentUserId Long managerUserId) {

        ManagerDashboardPageResponseDto response = managerDashboardService.getDashboardPage(managerUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * 운영자 대시보드 요약
     */
    @GetMapping("/summary")

    public ResponseEntity<ManagerDashboardSummaryResponseDto> getSummary(@CurrentUserId Long managerUserId) {
        ManagerDashboardSummaryResponseDto response = managerDashboardService.getDashboardSummary(managerUserId);
        return ResponseEntity.ok(response);
    }


}