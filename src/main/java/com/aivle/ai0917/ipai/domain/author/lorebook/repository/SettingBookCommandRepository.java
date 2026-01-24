package com.aivle.ai0917.ipai.domain.author.lorebook.repository;

import com.aivle.ai0917.ipai.domain.author.lorebook.model.SettingBookView; // 엔티티 임포트
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

// Object를 SettingBookView로 변경
public interface SettingBookCommandRepository extends Repository<SettingBookView, String> {

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO setting (userid, title, tag, keyword, episode, subtitle, settings)
        VALUES (:userid, :title, :tag, :keyword, :episode, :subtitle, CAST(:settings AS json))
        """, nativeQuery = true)
    void insert(
            @Param("userid") String[] userid,
            @Param("title") String title,
            @Param("tag") String tag,
            @Param("keyword") String keyword,
            @Param("episode") Integer[] episode,
            @Param("subtitle") String subtitle,
            @Param("settings") String settings
    );

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE setting
        SET title = :title,
            keyword = :keyword,
            subtitle = :subtitle,
            settings = CAST(:settings AS json)
        WHERE id = :id
        """, nativeQuery = true)
    int update(
            @Param("id") UUID id,
            @Param("title") String title,
            @Param("keyword") String keyword,
            @Param("subtitle") String subtitle,
            @Param("settings") String settings
    );

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM setting WHERE id = :id", nativeQuery = true)
    int delete(@Param("id") UUID id);
}