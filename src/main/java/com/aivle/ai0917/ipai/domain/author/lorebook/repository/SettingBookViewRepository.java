// SettingBookViewRepository.java
package com.aivle.ai0917.ipai.domain.author.lorebook.repository;

import com.aivle.ai0917.ipai.domain.author.lorebook.model.SettingBookView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SettingBookViewRepository extends JpaRepository<SettingBookView, UUID> {

    @Query(value = """
        SELECT * FROM v_setting_read
        WHERE :userId = ANY(userid)
          AND title = :title
        """, nativeQuery = true)
    Page<SettingBookView> findByUserIdAndTitle(
            @Param("userId") String userId,
            @Param("title") String title,
            Pageable pageable
    );

    @Query(value = """
        SELECT * FROM v_setting_read
        WHERE :userId = ANY(userid)
          AND title = :title
          AND tag = :tag
        """, nativeQuery = true)
    List<SettingBookView> findByUserIdAndTitleAndTag(
            @Param("userId") String userId,
            @Param("title") String title,
            @Param("tag") String tag
    );
}
