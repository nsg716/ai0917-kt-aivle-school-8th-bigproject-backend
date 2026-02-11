package com.aivle.ai0917.ipai.domain.manager.info.service;

import com.aivle.ai0917.ipai.domain.manager.info.dto.ManagerNoticeDto;
import com.aivle.ai0917.ipai.domain.manager.info.dto.ManagerNoticeDto.ManagerNoticeSource;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface ManagerNoticeService {

    // SSE 구독
    SseEmitter subscribe(String integrationId);

    // 알림 전송 (String ID 사용)
    void sendNotice(String integrationId, ManagerNoticeSource source, String title, String message, String url);

    // 알림 목록 조회
    List<ManagerNoticeDto> getNotices(String integrationId, boolean onlyUnread);

    // 읽음 처리
    void markAsRead(String integrationId, Long noticeId);

    // 모두 읽음 처리
    void markAllAsRead(String integrationId);
}