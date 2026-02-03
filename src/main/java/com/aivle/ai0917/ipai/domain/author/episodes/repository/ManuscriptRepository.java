package com.aivle.ai0917.ipai.domain.author.episodes.repository;

import com.aivle.ai0917.ipai.domain.author.episodes.model.ManuscriptView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ManuscriptRepository extends JpaRepository<ManuscriptView, Long> {
    Page<ManuscriptView> findByUserIdAndTitle(String userId, String title, Pageable pageable);

    Page<ManuscriptView> findByUserIdAndTitleAndTitleContaining(String userId, String title, String keyword, Pageable pageable);

    long countByWorkId(Long workId);
}