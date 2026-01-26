// ManuscriptCommandRepository.java
package com.aivle.ai0917.ipai.domain.author.manuscript.repository;

import com.aivle.ai0917.ipai.domain.author.manuscript.model.ManuscriptView;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ManuscriptCommandRepository extends Repository<ManuscriptView, UUID> {

    @Modifying
    @Transactional
    @Query(
            value = """
        INSERT INTO original (id, user_id, title, episode, subtitle, txt)
        VALUES (:id, :userId, :title, :episode, :subtitle, :txt)
        """,
            nativeQuery = true
    )
    int insert(
            @Param("id") UUID id,
            @Param("userId") String userId,
            @Param("title") String title,
            @Param("episode") Integer episode,
            @Param("subtitle") String subtitle,
            @Param("txt") String txt
    );

    @Modifying
    @Transactional
    @Query(
            value = "DELETE FROM original WHERE id = :id",
            nativeQuery = true
    )
    int deleteById(@Param("id") UUID id);
}
