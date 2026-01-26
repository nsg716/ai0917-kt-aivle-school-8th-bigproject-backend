// ManuscriptRepository.java
package com.aivle.ai0917.ipai.domain.author.manuscript.repository;

import com.aivle.ai0917.ipai.domain.author.manuscript.model.ManuscriptView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ManuscriptRepository extends JpaRepository<ManuscriptView, UUID> {

    Page<ManuscriptView> findByUserIdAndTitle(
            String userId, String title, Pageable pageable
    );

    Page<ManuscriptView> findByUserIdAndTitleAndTitleContaining(
            String userId, String title, String keyword, Pageable pageable
    );

    Optional<ManuscriptView> findById(UUID id);
}
