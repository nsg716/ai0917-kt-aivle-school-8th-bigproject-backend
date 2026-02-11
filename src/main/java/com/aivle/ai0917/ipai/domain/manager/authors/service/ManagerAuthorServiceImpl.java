package com.aivle.ai0917.ipai.domain.manager.authors.service;

import com.aivle.ai0917.ipai.domain.admin.access.model.UserRole;
import com.aivle.ai0917.ipai.domain.author.invitecode.service.InviteCodeService;
import com.aivle.ai0917.ipai.domain.user.model.User;
import com.aivle.ai0917.ipai.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aivle.ai0917.ipai.domain.manager.info.dto.ManagerNoticeDto; // [추가]
import com.aivle.ai0917.ipai.domain.manager.info.service.ManagerNoticeService;

import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ManagerAuthorServiceImpl implements ManagerAuthorService {

    private final UserRepository userRepository;
    private final InviteCodeService inviteCodeService;
    private final ManagerNoticeService managerNoticeService;

    public ManagerAuthorServiceImpl(UserRepository userRepository,
                                    InviteCodeService inviteCodeService, ManagerNoticeService managerNoticeService) {
        this.userRepository = userRepository;
        this.inviteCodeService = inviteCodeService;
        this.managerNoticeService = managerNoticeService;
    }

    @Override
    @Transactional
    public Map<String, Object> matchAuthorByInviteCode(Long managerUserId, String code) {

        User manager = userRepository.findById(managerUserId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (manager.getRole() != UserRole.Manager) {
            throw new RuntimeException("매니저(Manager)만 매칭을 등록할 수 있습니다.");
        }

        // ✅ 코드 소비 -> 작가 integrationId 획득(1회용)
        String authorIntegrationId = inviteCodeService.consumeValidCodeOrThrow(code);

        User author = userRepository.findByIntegrationId(authorIntegrationId)
                .orElseThrow(() -> new RuntimeException("작가를 찾을 수 없습니다."));

        if (author.getRole() != UserRole.Author) {
            throw new RuntimeException("해당 코드는 작가 계정이 아닙니다.");
        }

        // 작가당 매니저 1명 정책
        if (author.getManagerIntegrationId() != null && !author.getManagerIntegrationId().isBlank()) {
            throw new RuntimeException("이미 매니저와 매칭된 작가입니다.");
        }

        // ✅ 작가에게만 매니저 integrationId 등록
        author.setManagerIntegrationId(manager.getIntegrationId());
        userRepository.save(author);

        managerNoticeService.sendNotice(
                manager.getIntegrationId(), // 수신자: 매니저
                ManagerNoticeDto.ManagerNoticeSource.AUTHOR_PROPOSAL,
                "새로운 작가 연결",
                "작가 '" + author.getName() + "' 님과 매칭되었습니다.",
                "/manager/authors/" + author.getId() // 클릭 시 작가 상세 페이지 이동
        );
        return Map.of(
                "ok", true,
                "authorIntegrationId", author.getIntegrationId(),
                "managerIntegrationId", manager.getIntegrationId()
        );
    }
}
