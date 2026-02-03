package com.aivle.ai0917.ipai.domain.author.episodes.repository;

import com.aivle.ai0917.ipai.domain.author.episodes.model.ManuscriptView;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface ManuscriptCommandRepository extends Repository<ManuscriptView, Long> {

    @Modifying
    @Transactional
    @Query(
            value = """
        INSERT INTO episodes (
            user_id, work_id, title, ep_num, subtitle, txt_path, word_count,
            is_analyzed, created_at, updated_at
        )
        VALUES (
            :userId, :workId, :title, :episode, :subtitle, :txtPath, :wordCount,
            false, NOW(), NOW()
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

    // [수정] 삭제 시 updated_at도 함께 갱신 (트리거 제거 대응)
    @Modifying
    @Transactional
    @Query(
            value = """
            UPDATE episodes 
            SET deleted_at = NOW(),
                updated_at = NOW()
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
}