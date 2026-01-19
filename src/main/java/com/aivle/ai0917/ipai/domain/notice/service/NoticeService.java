package com.aivle.ai0917.ipai.domain.notice.service;

import com.aivle.ai0917.ipai.domain.notice.dto.NoticeRequestDto;
import com.aivle.ai0917.ipai.domain.notice.dto.NoticeResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface NoticeService {
    Page<NoticeResponseDto> getNoticeList(String keyword, Pageable pageable);
    NoticeResponseDto getNotice(Long id);
    Long createNotice(NoticeRequestDto request, MultipartFile file) throws IOException;
    void updateNotice(Long id, NoticeRequestDto request, MultipartFile file) throws IOException;
    void deleteNotice(Long id);
    void deleteNoticeFile(Long id);
}