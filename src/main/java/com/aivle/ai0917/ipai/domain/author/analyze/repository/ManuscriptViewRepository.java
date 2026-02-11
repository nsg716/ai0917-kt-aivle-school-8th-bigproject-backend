package com.aivle.ai0917.ipai.domain.author.analyze.repository;

import com.aivle.ai0917.ipai.domain.author.episodes.model.ManuscriptView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManuscriptViewRepository extends JpaRepository<ManuscriptView, Long> {

    // workId가 일치하고, isReadOnly가 true인 데이터를 회차(episode) 오름차순으로 조회
    List<ManuscriptView> findByWorkIdAndIsReadOnlyTrueOrderByEpisodeAsc(Long workId);
}