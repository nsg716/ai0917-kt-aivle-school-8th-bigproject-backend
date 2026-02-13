package com.aivle.ai0917.ipai.global.security.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;

@Service
public class RedisTokenBlacklistService implements TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(RedisTokenBlacklistService.class);
    private static final String KEY_PREFIX = "jwt:blacklist:";

    private final StringRedisTemplate redisTemplate;

    public RedisTokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void blacklist(String token, Duration ttl) {
        if (token == null || token.isBlank()) return;
        if (ttl == null || ttl.isNegative() || ttl.isZero()) return;

        try {
            String key = redisKey(token);
            redisTemplate.opsForValue().set(key, "1", ttl);
        } catch (Exception e) {
            log.error("Failed to write token blacklist to Redis", e);
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) return false;

        try {
            String key = redisKey(token);
            Boolean hasKey = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(hasKey);
        } catch (Exception e) {
            log.error("Failed to read token blacklist from Redis", e);
            return false;
        }
    }

    private String redisKey(String token) {
        return KEY_PREFIX + sha256(token);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }
}