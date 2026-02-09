package com.aivle.ai0917.ipai.domain.author.lorebook.repository;

import com.aivle.ai0917.ipai.domain.author.lorebook.model.SettingBookView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SettingBookViewRepository extends JpaRepository<SettingBookView, Long> {

    // [수정] :userId = ANY(user_id) 로 변경 (JPA Native Query 사용 권장 또는 JPQL 커스텀)
    // JPQL에서 배열 타입 매핑이 까다로우므로 Native Query 사용을 추천합니다.

    @Query(value = "SELECT * FROM active_lorebooks_view WHERE :userId = ANY(user_id) AND work_id = :workId",
            countQuery = "SELECT count(*) FROM active_lorebooks_view WHERE :userId = ANY(user_id) AND work_id = :workId",
            nativeQuery = true)
    Page<SettingBookView> findByUserIdAndWorkId(@Param("userId") String userId, @Param("workId") Long workId, Pageable pageable);

    @Query(value = "SELECT * FROM active_lorebooks_view WHERE :userId = ANY(user_id) AND work_id = :workId AND category = :category",
            nativeQuery = true)
    List<SettingBookView> findByUserIdAndWorkIdAndCategory(@Param("userId") String userId, @Param("workId") Long workId, @Param("category") String category);

    /**
     * 작품 ID 목록으로 설정집 조회
     * @param workId
     * @return
     */
    List<SettingBookView> findAllByWorkId(Long workId);


}