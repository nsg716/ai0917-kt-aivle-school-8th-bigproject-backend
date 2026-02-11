package com.aivle.ai0917.ipai.domain.manager.info.repository;

import com.aivle.ai0917.ipai.domain.manager.info.model.ManagerNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ManagerNoticeRepository extends JpaRepository<ManagerNotice, Long> {

    // [수정] Long -> String
    List<ManagerNotice> findByManagerIdOrderByCreatedAtDesc(String managerId);

    // [수정] Long -> String
    List<ManagerNotice> findByManagerIdAndIsReadFalseOrderByCreatedAtDesc(String managerId);

    // [수정] Long -> String
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ManagerNotice n SET n.isRead = true WHERE n.managerId = :managerId AND n.isRead = false")
    void markAllAsRead(@Param("managerId") String managerId);
}