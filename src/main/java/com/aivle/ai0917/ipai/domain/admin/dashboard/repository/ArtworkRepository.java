package com.aivle.ai0917.ipai.domain.admin.dashboard.repository;

import com.aivle.ai0917.ipai.domain.author.works.model.Work;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ArtworkRepository extends JpaRepository<Work, Long> {

    /**
     * 저장된 작품 수 조회
     * 수정사항: active_works_view 뷰 테이블을 기준으로 카운트 (Native Query 사용)
     */
    @Query(value = "SELECT COUNT(*) FROM active_works_view", nativeQuery = true)
    long countSaveArtworks();
}