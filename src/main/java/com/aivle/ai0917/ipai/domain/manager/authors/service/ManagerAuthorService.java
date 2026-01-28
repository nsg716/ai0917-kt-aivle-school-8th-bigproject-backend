package com.aivle.ai0917.ipai.domain.manager.authors.service;

import java.util.Map;

public interface ManagerAuthorService {
    Map<String, Object> matchAuthorByInviteCode(Long managerUserId, String code);
}
