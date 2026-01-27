package com.aivle.ai0917.ipai.domain.admin.access.dto;

import com.aivle.ai0917.ipai.domain.admin.access.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 사용자 목록 조회 응답 (테이블용)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponseDto {
    private Long id;
    private String name;
    private String siteEmail;
    private UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivityAt;
}
