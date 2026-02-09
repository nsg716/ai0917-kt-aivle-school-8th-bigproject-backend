package com.aivle.ai0917.ipai.domain.author.episodes.repository;

import com.aivle.ai0917.ipai.domain.author.episodes.model.ManuscriptView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ManuscriptRepository extends JpaRepository<ManuscriptView, Long> {
    Page<ManuscriptView> findByUserIdAndTitle(String userId, String title, Pageable pageable);

    Page<ManuscriptView> findByUserIdAndTitleAndTitleContaining(String userId, String title, String keyword, Pageable pageable);

    long countByWorkId(Long workId);

    Optional<ManuscriptView> findByWorkIdAndEpisode(Long workId, Integer episode);

    @Query(
            value = "SELECT MAX(ep_num) FROM episodes WHERE work_id = :workId AND deleted_at IS NULL",
            nativeQuery = true
    )
    Integer findMaxEpisodeByWorkId(@Param("workId") Long workId);

    boolean existsByWorkIdAndIsReadOnlyFalse(Long workId);


}