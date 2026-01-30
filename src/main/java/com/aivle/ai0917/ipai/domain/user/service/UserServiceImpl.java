package com.aivle.ai0917.ipai.domain.user.service;

import com.aivle.ai0917.ipai.domain.admin.access.model.UserRole;
import com.aivle.ai0917.ipai.domain.user.model.User;
import com.aivle.ai0917.ipai.domain.user.repository.UserRepository;
import com.aivle.ai0917.ipai.global.utils.Base62Util;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // ✅ 추가: 비밀번호 인코딩은 Service에서 담당
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<User> getUserByIntegrationId(String integrationId) {
        return userRepository.findByIntegrationId(integrationId);
    }

    // ✅ 추가: 리포지토리의 existsBySiteEmail 호출
    @Override
    public boolean existsBySiteEmail(String siteEmail) {
        return userRepository.existsBySiteEmail(siteEmail);
    }
    // ✅ 추가
    @Override
    public Optional<User> getUserByNameAndSiteEmail(String name, String siteEmail) {
        return userRepository.findByNameAndSiteEmail(name, siteEmail);
    }

    // ✅ 추가
    @Override
    public Optional<User> getUserBySiteEmail(String siteEmail) {
        return userRepository.findBySiteEmail(siteEmail);
    }

    // ✅ 추가: 비번 변경은 트랜잭션으로 묶어서 안전하게
    @Override
    @Transactional
    public void updateSitePassword(String siteEmail, String rawNewPassword) {
        User user = userRepository.findBySiteEmail(siteEmail)
                .orElseThrow(() -> new RuntimeException("해당 이메일의 사용자를 찾을 수 없습니다."));

        user.setSitePwd(passwordEncoder.encode(rawNewPassword));
        userRepository.save(user);
    }


    @Override
    @Transactional
    public User registerUser(User user) {
        // 8자리 ID 수동 할당 로직 (중복 방지 강화 버전)
        String uniqueId = Base62Util.generate8CharId();
        while (userRepository.findByIntegrationId(uniqueId).isPresent()) {
            uniqueId = Base62Util.generate8CharId();
        }
        user.setIntegrationId(uniqueId);

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deactivated(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.setRole(UserRole.Deactivated);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePasswordAfterLogin(Long userId, String currentPassword, String newPassword) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (user.getSitePwd() == null || user.getSitePwd().isBlank()) {
            throw new RuntimeException("이 계정은 비밀번호가 설정되어 있지 않습니다.");
        }

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(currentPassword, user.getSitePwd())) {
            throw new RuntimeException("현재 비밀번호가 올바르지 않습니다.");
        }

        // 새 비밀번호 저장
        user.setSitePwd(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

}