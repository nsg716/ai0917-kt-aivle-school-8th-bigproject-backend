package com.aivle.ai0917.ipai.domain.admin.access.dto;

import com.aivle.ai0917.ipai.domain.admin.access.model.UserRole;
import lombok.Getter;
import lombok.Setter;

// 사용자 권한 수정 요청
@Getter
@Setter
public class UserUpdateRequestDto {
    private UserRole role;
}
