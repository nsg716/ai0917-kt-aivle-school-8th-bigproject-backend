package com.aivle.ai0917.ipai.global.security.token;

import java.time.Duration;

public interface TokenBlacklistService {
    void blacklist(String token, Duration ttl);

    boolean isBlacklisted(String token);
}