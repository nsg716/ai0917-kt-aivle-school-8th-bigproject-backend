package com.aivle.ai0917.ipai.infra.naver.dto;

public record ChangePasswordRequest(
        String currentPassword,
        String newPassword,
        String newPasswordConfirm
) {}