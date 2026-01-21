package com.aivle.ai0917.ipai.domain.admin.access.dto;

import com.aivle.ai0917.ipai.domain.admin.access.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 새 사용자 추가 요청
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequestDto {
    private String name;
    private String mobile;
    private UserRole role;
    private String siteEmail;
    private String sitePwd;
}