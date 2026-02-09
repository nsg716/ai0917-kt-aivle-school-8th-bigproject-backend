package com.aivle.ai0917.ipai.domain.author.lorebook.repository;

import com.aivle.ai0917.ipai.domain.author.lorebook.model.SettingBookView;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SettingBookCommandRepository extends Repository<SettingBookView, Long> {

    // [수정] user_id 저장 시 ARRAY[:userId]로 감싸서 배열로 저장
    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO lorebooks (
            user_id, work_id, category, keyword, setting, ep_num, created_at, updated_at
        )
        VALUES (
            ARRAY[:userId]::varchar[], :workId, :category, :keyword, CAST(:setting AS jsonb), :epNum, NOW(), NOW()
        )
        """, nativeQuery = true)
    void insert(
            @Param("userId") String userId,
            @Param("workId") Long workId,
            @Param("category") String category,
            @Param("keyword") String keyword,
            @Param("setting") String setting,
            @Param("epNum") Integer[] epNum
    );

    // [수정] user_id 비교 시 = ANY(...) 사용 (배열 안에 해당 유저가 있는지 확인)
    @Modifying
    @Transactional
    @Query(value = """
        UPDATE lorebooks
        SET keyword = :keyword,
            setting = CAST(:setting AS jsonb),
            updated_at = NOW()
        WHERE id = :id AND :userId = ANY(user_id)
        """, nativeQuery = true)
    int update(
            @Param("id") Long id,
            @Param("userId") String userId,
            @Param("keyword") String keyword,
            @Param("setting") String setting
    );

    // 삭제는 ID 기준이므로 변경 없음 (Soft Delete)
    @Modifying
    @Transactional
    @Query(value = "UPDATE lorebooks SET deleted_at = NOW() WHERE id = :id", nativeQuery = true)
    int delete(@Param("id") Long id);


    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM lorebooks WHERE deleted_at <= :threshold", nativeQuery = true)
    long deleteByDeletedAtBefore(@Param("threshold") LocalDateTime threshold);
}