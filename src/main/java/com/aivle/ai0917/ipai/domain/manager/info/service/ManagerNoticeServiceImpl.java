package com.aivle.ai0917.ipai.domain.manager.info.service;

import com.aivle.ai0917.ipai.domain.manager.info.dto.ManagerNoticeDto;
import com.aivle.ai0917.ipai.domain.manager.info.dto.ManagerNoticeDto.ManagerNoticeSource;
import com.aivle.ai0917.ipai.domain.manager.info.model.ManagerNotice;
import com.aivle.ai0917.ipai.domain.manager.info.repository.ManagerNoticeRepository;
import com.aivle.ai0917.ipai.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerNoticeServiceImpl implements ManagerNoticeService {

    private final ManagerNoticeRepository managerNoticeRepository;
    private final UserRepository userRepository; // 유효성 검사 용도로 남겨둠

    // [수정] Map Key를 String으로 변경
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private static final long SSE_TIMEOUT = 60 * 60 * 1000L;

    @Override
    public SseEmitter subscribe(String integrationId) {
        // 유효한 유저인지 검증은 필요하다면 수행 (없으면 예외 발생)
        if (!userRepository.existsByIntegrationId(integrationId)) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다: " + integrationId);
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.put(integrationId, emitter); // String Key 사용

        emitter.onCompletion(() -> emitters.remove(integrationId));
        emitter.onTimeout(() -> emitters.remove(integrationId));
        emitter.onError((e) -> emitters.remove(integrationId));

        sendToClient(integrationId, "connected", "SSE Connected [" + integrationId + "]");
        return emitter;
    }

    @Override
    @Transactional
    public void sendNotice(String integrationId, ManagerNoticeSource source, String title, String message, String url) {
        // 1. DB 저장 (String ID 그대로 저장)
        ManagerNotice notice = managerNoticeRepository.save(ManagerNotice.builder()
                .managerId(integrationId) // String
                .source(source.name())
                .title(title)
                .message(message)
                .redirectUrl(url)
                .build());

        // 2. 실시간 전송 (String Key로 찾아서 전송)
        if (emitters.containsKey(integrationId)) {
            ManagerNoticeDto dto = convertToDto(notice);
            sendToClient(integrationId, "manager-notice", dto);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ManagerNoticeDto> getNotices(String integrationId, boolean onlyUnread) {
        List<ManagerNotice> list = onlyUnread ?
                managerNoticeRepository.findByManagerIdAndIsReadFalseOrderByCreatedAtDesc(integrationId) :
                managerNoticeRepository.findByManagerIdOrderByCreatedAtDesc(integrationId);

        return list.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(String integrationId, Long noticeId) {
        ManagerNotice notice = managerNoticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다."));

        // 본인 확인 (String 비교)
        if (!notice.getManagerId().equals(integrationId)) {
            throw new SecurityException("본인의 알림만 읽을 수 있습니다.");
        }
        notice.markAsRead();
    }

    @Override
    @Transactional
    public void markAllAsRead(String integrationId) {
        managerNoticeRepository.markAllAsRead(integrationId);
    }

    // --- Helper Methods ---

    private void sendToClient(String integrationId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(integrationId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                emitters.remove(integrationId);
            }
        }
    }

    private ManagerNoticeDto convertToDto(ManagerNotice entity) {
        return ManagerNoticeDto.builder()
                .id(entity.getId())
                .source(ManagerNoticeSource.valueOf(entity.getSource()))
                .title(entity.getTitle())
                .message(entity.getMessage())
                .isRead(entity.isRead())
                .createdAt(entity.getCreatedAt())
                .redirectUrl(entity.getRedirectUrl())
                .build();
    }
}