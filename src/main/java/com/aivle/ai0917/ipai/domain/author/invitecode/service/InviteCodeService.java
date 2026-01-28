package com.aivle.ai0917.ipai.domain.author.invitecode.service;

import java.util.Map;

public interface InviteCodeService {
    Map<String, Object> createInviteCode(Long authorUserId);

    /** 매니저가 코드 입력할 때: 유효하면 authorIntegrationId 반환(1회용) */
    String consumeValidCodeOrThrow(String code);
}