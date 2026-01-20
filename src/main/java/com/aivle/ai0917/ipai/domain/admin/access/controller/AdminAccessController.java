package com.aivle.ai0917.ipai.domain.admin.access.controller;

import com.aivle.ai0917.ipai.domain.admin.access.dto.*;
import com.aivle.ai0917.ipai.domain.admin.access.model.UserRole;
import com.aivle.ai0917.ipai.domain.admin.access.service.AdminAccessService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/access")
@RequiredArgsConstructor
public class AdminAccessController {

    private final AdminAccessService adminAccessService;

    // 권한별 사용자 요약 조회
    @GetMapping("/summary")
    public ResponseEntity<AccessSummaryResponseDto> getSummary() {
        return ResponseEntity.ok(adminAccessService.getAccessSummary());
    }

    // 사용자 목록 조회 및 검색
    @GetMapping("/users")
    public ResponseEntity<UserPageResponse> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserRole role,
            Pageable pageable) {

        return ResponseEntity.ok(new UserPageResponse(adminAccessService.getUserList(keyword, role, pageable)));
    }

    // 사용자 상세 권한 조회
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDetailResponseDto> getUserDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminAccessService.getUserDetail(id));
    }

    // 새 사용자 추가 및 권한 부여
    @PostMapping("/users")
    public ResponseEntity<Void> createUser(@RequestBody UserCreateRequestDto request) {
        adminAccessService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 사용자 권한 정보 수정
    @PatchMapping("/users/{id}")
    public ResponseEntity<Void> updateUserRole(
            @PathVariable Long id,
            @RequestBody UserUpdateRequestDto request) {
        adminAccessService.updateUserRole(id, request);
        return ResponseEntity.ok().build();
    }

    // 사용자 권한 회수 및 삭제
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminAccessService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
