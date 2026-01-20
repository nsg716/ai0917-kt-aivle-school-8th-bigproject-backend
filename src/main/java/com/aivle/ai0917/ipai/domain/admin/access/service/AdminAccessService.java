package com.aivle.ai0917.ipai.domain.admin.access.service;

import com.aivle.ai0917.ipai.domain.admin.access.dto.*;
import com.aivle.ai0917.ipai.domain.admin.access.model.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminAccessService {
    AccessSummaryResponseDto getAccessSummary();
    Page<UserListResponseDto> getUserList(String keyword, UserRole role, Pageable pageable);
    UserDetailResponseDto getUserDetail(Long id);
    void createUser(UserCreateRequestDto request);
    void updateUserRole(Long id, UserUpdateRequestDto request);
    void deleteUser(Long id);
}