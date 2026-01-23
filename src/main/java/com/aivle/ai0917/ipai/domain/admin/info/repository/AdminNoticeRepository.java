package com.aivle.ai0917.ipai.domain.admin.info.repository;

import com.aivle.ai0917.ipai.domain.admin.info.model.AdminNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminNoticeRepository extends JpaRepository<AdminNotice, Long> {

    // 특정 타겟 역할에 맞는 최신 알림 5개 조회
    List<AdminNotice> findTop5ByTargetRoleOrderByCreatedAtDesc(String targetRole);

    /**
     * 특정 시간 이후의 알림 조회 (통합 알림용)
     */
    List<AdminNotice> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime since);

    /**
     * 특정 역할 + 특정 시간 이후의 알림 조회
     */
    List<AdminNotice> findByTargetRoleAndCreatedAtAfterOrderByCreatedAtDesc(
            String targetRole, LocalDateTime since);

    /**
     * 읽지 않은 알림 개수 조회
     */
    long countByIsReadFalseAndTargetRole(String targetRole);

    /**
     * 읽지 않은 모든 알림 조회
     */
    List<AdminNotice> findByIsReadFalseOrderByCreatedAtDesc();

    /**
     * 모든 알림 읽음 처리
     */
    @Modifying
    @Query("UPDATE AdminNotice n SET n.isRead = true WHERE n.isRead = false")
    void markAllAsRead();
}