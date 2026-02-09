package com.aivle.ai0917.ipai.domain.author.episodes.repository;

import com.aivle.ai0917.ipai.domain.author.episodes.model.ManuscriptView;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ManuscriptCommandRepository extends Repository<ManuscriptView, Long> {

    @Modifying
    @Transactional
    @Query(
            value = """
        INSERT INTO episodes (
            user_id, work_id, title, ep_num, subtitle, txt_path, word_count,
            created_at, updated_at
        )
        VALUES (
            :userId, :workId, :title, :episode, :subtitle, :txtPath, :wordCount,
            NOW(), NOW()
        )
        """,
            nativeQuery = true
    )
    void insert(
            @Param("userId") String userId,
            @Param("workId") Long workId,
            @Param("title") String title,
            @Param("episode") Integer episode,
            @Param("subtitle") String subtitle,
            @Param("txtPath") String txtPath,
            @Param("wordCount") Integer wordCount
    );

    @Modifying
    @Transactional
    @Query(
            value = "UPDATE episodes SET txt_path = :txtPath, updated_at = NOW() WHERE id = :id",
            nativeQuery = true
    )
    int updateTxtPath(
            @Param("id") Long id,
            @Param("txtPath") String txtPath
    );

    // [수정] txt_path를 NULL로 바꾸지 않음 (NOT NULL 제약 회피)
    // 대신 deleted_at 설정 및 word_count를 0으로 초기화
    @Modifying
    @Transactional
    @Query(
            value = """
            UPDATE episodes 
            SET deleted_at = NOW(),
                updated_at = NOW(),
                word_count = 0
            WHERE id = :id
            """,
            nativeQuery = true
    )
    int deleteById(@Param("id") Long id);

    // [수정] 텍스트 경로(txt_path)와 글자 수(word_count)도 수정 가능하도록 변경
    @Modifying
    @Transactional
    @Query(
            value = """
            UPDATE episodes 
            SET subtitle = COALESCE(:subtitle, subtitle),
                ep_num = COALESCE(:epNum, ep_num),
                txt_path = COALESCE(:txtPath, txt_path),
                word_count = COALESCE(:wordCount, word_count),
                updated_at = NOW() 
            WHERE id = :id
        """,
            nativeQuery = true
    )
    int updateManuscript(
            @Param("id") Long id,
            @Param("subtitle") String subtitle,
            @Param("epNum") Integer epNum,
            @Param("txtPath") String txtPath,     // [추가]
            @Param("wordCount") Integer wordCount // [추가]
    );

    // [추가] 삭제된 회차 이후의 에피소드 번호들을 1씩 감소 (재정렬)
    @Modifying
    @Transactional
    @Query(
            value = """
            UPDATE episodes
            SET ep_num = ep_num - 1
            WHERE work_id = :workId
              AND ep_num > :deletedEpNum
              AND deleted_at IS NULL
            """,
            nativeQuery = true
    )
    void reorderEpisodesAfterDeletion(
            @Param("workId") Long workId,
            @Param("deletedEpNum") Integer deletedEpNum
    );

    // [추가] 에피소드 목록을 읽기 전용(is_read_only = true)으로 변경
    @Modifying
    @Transactional
    @Query(
            value = """
            UPDATE episodes
            SET is_read_only = true,
                updated_at = NOW()
            WHERE id IN (:ids)
            """,
            nativeQuery = true
    )
    void updateIsReadOnlyTrue(@Param("ids") List<Long> ids);

    @Query(value = """
        SELECT count(*) > 0 
        FROM episodes 
        WHERE work_id = :workId 
          AND is_read_only = false 
          AND deleted_at IS NULL 
          AND ep_num != :currentEpisode
        """, nativeQuery = true)
    boolean existsPendingAnalysisExcludingCurrent(@Param("workId") Long workId, @Param("currentEpisode") Integer currentEpisode);



    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM episodes WHERE deleted_at <= :threshold", nativeQuery = true)
    long deleteByDeletedAtBefore(@Param("threshold") LocalDateTime threshold);
}