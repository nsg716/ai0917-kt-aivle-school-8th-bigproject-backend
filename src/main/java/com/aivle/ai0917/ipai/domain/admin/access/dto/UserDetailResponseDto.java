package com.aivle.ai0917.ipai.domain.admin.access.dto;

import com.aivle.ai0917.ipai.domain.admin.access.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class UserDetailResponseDto {
    private Long id;
    private String name;
    private String email;
    private String mobile;
    private UserRole role;
    private String birthYear;
    private String gender;
}