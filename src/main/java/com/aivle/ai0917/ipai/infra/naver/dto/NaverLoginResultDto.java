package com.aivle.ai0917.ipai.infra.naver.dto;

import com.aivle.ai0917.ipai.domain.user.model.User;

/**
 * 네이버 로그인 결과 DTO
 * - 신규 회원 여부와 사용자 정보를 함께 반환
 */
public record NaverLoginResultDto(
        User user,
        boolean isNewMember
) {
}