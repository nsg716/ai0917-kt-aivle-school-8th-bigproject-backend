package com.aivle.ai0917.ipai.domain.admin.dashboard.repository;

import com.aivle.ai0917.ipai.domain.author.lorebook.model.SettingBookView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LorebookRepository extends JpaRepository<SettingBookView, Long> {

    /**
     * 저장된 설정집 수 조회
     * active_lorebooks_view 뷰 테이블 기준
     */
    @Query(value = "SELECT COUNT(*) FROM active_lorebooks_view", nativeQuery = true)
    long countSavedLorebooks();
}