package com.aivle.ai0917.ipai.infra.naver.service;


import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailVerificationService {

    private final JavaMailSender mailSender;
    private final SecureRandom random = new SecureRandom();

    // ✅ 로컬 개발용: 메모리 저장소
    // ⚠️ 서버 재시작하면 인증 정보 사라짐
    // ✅ AWS 운영: Redis 권장 (TTL 관리 쉬움)
    private final Map<String, CodeEntry> store = new ConcurrentHashMap<>();

    public EmailVerificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendCode(String email) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        Instant expiresAt = Instant.now().plusSeconds(5 * 60); // 5분 유효

        store.put(email, new CodeEntry(code, expiresAt, false));

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("[IPAI] 이메일 인증 코드");
        msg.setText("인증 코드: " + code + "\n(5분 내 입력)");
        mailSender.send(msg);
    }

    public boolean verifyCode(String email, String code) {
        CodeEntry entry = store.get(email);
        if (entry == null) return false;
        if (Instant.now().isAfter(entry.expiresAt)) return false;
        if (!entry.code.equals(code)) return false;

        store.put(email, new CodeEntry(entry.code, entry.expiresAt, true));
        return true;
    }

    public boolean isVerified(String email) {
        CodeEntry entry = store.get(email);
        return entry != null && entry.verified && Instant.now().isBefore(entry.expiresAt);
    }

    private record CodeEntry(String code, Instant expiresAt, boolean verified) {}
}