package com.aivle.ai0917.ipai.domain.author.works.repository;

import com.aivle.ai0917.ipai.domain.author.works.model.Work;
import com.aivle.ai0917.ipai.domain.author.works.model.WorkStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkRepository extends JpaRepository<Work, Long> {

    // [수정] String status -> WorkStatus status
    List<Work> findAllByPrimaryAuthorIdAndStatusNotOrderByCreatedAtDesc(
            String primaryAuthorId, WorkStatus status
    );
    // 작가 작품 목록 (정렬: 제목)
    List<Work> findAllByPrimaryAuthorIdAndStatusNotOrderByTitleAsc(
            String primaryAuthorId, WorkStatus status
    );

    // ✅ 매니저 화면: 작품 수 카운트
    long countByPrimaryAuthorIdAndStatusNot(String primaryAuthorId, WorkStatus status);

    // ✅ 매니저 상세: 최근 작품 5개
    List<Work> findTop5ByPrimaryAuthorIdAndStatusNotOrderByCreatedAtDesc(String primaryAuthorId, WorkStatus status);


}
