package com.aivle.ai0917.ipai.domain.author.invitecode.service;

import com.aivle.ai0917.ipai.domain.admin.access.model.UserRole;
import com.aivle.ai0917.ipai.domain.author.invitecode.repository.InviteCodeRepository;
import com.aivle.ai0917.ipai.domain.user.model.User;
import com.aivle.ai0917.ipai.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class InviteCodeServiceImpl implements InviteCodeService {

    private final UserRepository userRepository;
    private final InviteCodeRepository inviteCodeRepository;
    private final SecureRandom random = new SecureRandom();

    public InviteCodeServiceImpl(UserRepository userRepository,
                                 InviteCodeRepository inviteCodeRepository) {
        this.userRepository = userRepository;
        this.inviteCodeRepository = inviteCodeRepository;
    }

    @Override
    @Transactional
    public Map<String, Object> createInviteCode(Long authorUserId) {
        User author = userRepository.findById(authorUserId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (author.getRole() != UserRole.Author) {
            throw new RuntimeException("작가(Author)만 코드를 생성할 수 있습니다.");
        }

        // 작가당 매니저 1명 정책: 이미 매칭된 작가면 발급 불가
        if (author.getManagerIntegrationId() != null && !author.getManagerIntegrationId().isBlank()) {
            throw new RuntimeException("이미 매니저와 매칭된 작가입니다.");
        }

        String authorIntegrationId = author.getIntegrationId();

        // 재발급이면 기존 코드 무효화
        inviteCodeRepository.deleteOldCodeOfAuthor(authorIntegrationId);

        // 6자리 코드 생성(충돌 방지)
        String code;
        do {
            code = String.format("%06d", random.nextInt(1_000_000));
        } while (inviteCodeRepository.existsByCode(code));

        Instant expiresAt = Instant.now().plusSeconds(5 * 60);

        inviteCodeRepository.save(code, authorIntegrationId, expiresAt);

        return Map.of(
                "ok", true,
                "code", code,
                "expiresAt", expiresAt.toString()
        );
    }

    @Override
    @Transactional
    public String consumeValidCodeOrThrow(String code) {
        var entry = inviteCodeRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 코드입니다."));

        if (Instant.now().isAfter(entry.expiresAt())) {
            inviteCodeRepository.deleteByCode(code);
            throw new RuntimeException("코드가 만료되었습니다. 작가에게 새 코드를 요청하세요.");
        }

        // 1회용 소비(재사용 방지)
        inviteCodeRepository.deleteByCode(code);

        return entry.authorIntegrationId();
    }
}
