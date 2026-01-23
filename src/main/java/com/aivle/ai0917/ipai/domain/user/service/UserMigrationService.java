//package com.aivle.ai0917.ipai.domain.user.service;
//
//import com.aivle.ai0917.ipai.domain.user.model.User;
//import com.aivle.ai0917.ipai.domain.user.repository.UserRepository;
//import com.aivle.ai0917.ipai.global.utils.Base62Util;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j; // 로그 추가
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class UserMigrationService {
//
//    private final UserRepository userRepository;
//
//    @Transactional
//    public void migrateUserIntegrationIds() {
//        log.info("=== 사용자 고유 ID 마이그레이션 시작 ===");
//
//        // 1. NULL 또는 빈 문자열인 사용자 모두 조회 (더 안전한 조건)
//        List<User> allUsers = userRepository.findAll();
//        List<User> usersToUpdate = allUsers.stream()
//                .filter(u -> u.getIntegrationId() == null || u.getIntegrationId().trim().isEmpty())
//                .toList();
//
//        log.info("대상 사용자 수: {} 명", usersToUpdate.size());
//
//        if (usersToUpdate.isEmpty()) {
//            log.info("마이그레이션할 대상이 없습니다.");
//            return;
//        }
//
//        for (User user : usersToUpdate) {
//            String newId;
//            // 중복되지 않는 ID 생성
//            do {
//                newId = Base62Util.generate8CharId();
//            } while (userRepository.findByIntegrationId(newId).isPresent());
//
//            user.setIntegrationId(newId);
//            log.debug("User ID {} 에 고유값 {} 할당", user.getId(), newId);
//        }
//
//        // 2. 명시적으로 저장 호출 (Dirty Checking에만 의존하지 않음)
//        userRepository.saveAll(usersToUpdate);
//
//        // 3. 강제 플러시 (트랜잭션 종료 전 DB 반영 확인용)
//        userRepository.flush();
//
//        log.info("=== 마이그레이션 완료: {} 명의 데이터가 업데이트 되었습니다. ===", usersToUpdate.size());
//    }
//}