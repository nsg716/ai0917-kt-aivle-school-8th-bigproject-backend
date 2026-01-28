package com.aivle.ai0917.ipai.domain.author.invitecode.repository;


import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InviteCodeRepository {

    // code -> (authorIntegrationId, expiresAt)
    private final Map<String, CodeEntry> codeStore = new ConcurrentHashMap<>();

    // authorIntegrationId -> code (재발급 시 기존 코드 무효화)
    private final Map<String, String> authorToCode = new ConcurrentHashMap<>();

    public void save(String code, String authorIntegrationId, Instant expiresAt) {
        codeStore.put(code, new CodeEntry(authorIntegrationId, expiresAt));
        authorToCode.put(authorIntegrationId, code);
    }

    public Optional<CodeEntry> findByCode(String code) {
        return Optional.ofNullable(codeStore.get(code));
    }

    public boolean existsByCode(String code) {
        return codeStore.containsKey(code);
    }


    public void deleteByCode(String code) {
        CodeEntry entry = codeStore.remove(code);
        if (entry != null) {
            authorToCode.remove(entry.authorIntegrationId(), code);
        }
    }

    public void deleteOldCodeOfAuthor(String authorIntegrationId) {
        String old = authorToCode.get(authorIntegrationId);
        if (old != null) {
            deleteByCode(old);
        }
    }

    public record CodeEntry(String authorIntegrationId, Instant expiresAt) {}
}