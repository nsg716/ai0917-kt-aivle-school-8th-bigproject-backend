package com.aivle.ai0917.ipai.domain.notice.repository;

import com.aivle.ai0917.ipai.domain.notice.model.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    // 필요한 경우 특정 작성자의 공지사항 찾기 등의 메서드 추가 가능
    Page<Notice> findByTitleContaining(String title, Pageable pageable);
}