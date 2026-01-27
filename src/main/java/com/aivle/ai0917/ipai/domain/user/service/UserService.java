package com.aivle.ai0917.ipai.domain.user.service;

import com.aivle.ai0917.ipai.domain.user.model.User;
import java.util.Optional;

public interface UserService {
    // 고유 ID로 사용자 찾기
    Optional<User> getUserByIntegrationId(String integrationId);

    // 사용자 등록 (이 과정에서 중복 체크 로직 포함 가능)
    User registerUser(User user);

    boolean existsBySiteEmail(String siteEmail);

   // ✅ 추가: 이름+이메일로 사용자 조회 (비번 찾기에서 사용)
    Optional<User> getUserByNameAndSiteEmail(String name, String siteEmail);

    // ✅ 추가: 이메일로 사용자 조회
    Optional<User> getUserBySiteEmail(String siteEmail);

    // ✅ 추가: 비밀번호 변경(내부에서 인코딩 + 저장까지)
    void updateSitePassword(String siteEmail, String rawNewPassword);

    // ✅ 추가: 계정 탈퇴 처리 */
    void deactivated(Long userId);

}
