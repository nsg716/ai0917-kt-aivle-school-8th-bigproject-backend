package com.aivle.ai0917.ipai.domain.admin.access.service;

import com.aivle.ai0917.ipai.domain.admin.access.dto.*;
import com.aivle.ai0917.ipai.domain.admin.access.model.UserRole;


import com.aivle.ai0917.ipai.domain.admin.access.repository.AccessUserRepository;
import com.aivle.ai0917.ipai.domain.user.model.User;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminAccessServiceImpl implements AdminAccessService {

    private final AccessUserRepository accessUserRepository;

    @Override
    public AccessSummaryResponseDto getAccessSummary() {
        return AccessSummaryResponseDto.builder()
                .adminCount(accessUserRepository.countByRole(UserRole.Admin))
                .managerCount(accessUserRepository.countByRole(UserRole.Manager))
                .authorCount(accessUserRepository.countByRole(UserRole.Author))
                .deactivatedCount(accessUserRepository.countByRole(UserRole.Deactivated))
                .build();
    }

    @Override
    public Page<UserListResponseDto> getUserList(String keyword, UserRole role, Pageable pageable) {


        // keyword가 빈 문자열("")이면 null로 처리하여 쿼리에서 무시되도록 함
        String searchKeyword = (keyword == null || keyword.isBlank()) ? null : keyword;

        // 위에서 만든 searchUsers 메서드 호출
        return accessUserRepository.searchUsers(searchKeyword, role, pageable)
                .map(user -> UserListResponseDto.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .siteEmail(user.getSiteEmail())
                        .role(user.getRole())
                        .createdAt(user.getCreatedAt())
                        .lastActivityAt(user.getLastActivityAt())
                        .build());
    }

    @Override
    public UserDetailResponseDto getUserDetail(Long id) {
        User user = accessUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserDetailResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .role(user.getRole())
                .birthYear(user.getBirthYear())
                .gender(user.getGender())
                .build();
    }

    @Transactional
    @Override
    public void createUser(UserCreateRequestDto request) {
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .siteEmail(request.getSiteEmail())
                .sitePwd(request.getSitePwd()) // 실제론 PasswordEncoder 사용 권장
                .role(request.getRole())
                .mobile(request.getMobile())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        accessUserRepository.save(user);
    }

    @Transactional
    @Override
    public void updateUserRole(Long id, UserUpdateRequestDto request) {
        User user = accessUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.updateAccess(request.getRole());
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        accessUserRepository.deleteById(id);
    }
}