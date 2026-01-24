// SettingBookServiceImpl.java
package com.aivle.ai0917.ipai.domain.author.lorebook.service;

import com.aivle.ai0917.ipai.domain.author.lorebook.dto.*;
import com.aivle.ai0917.ipai.domain.author.lorebook.model.SettingBookView;
import com.aivle.ai0917.ipai.domain.author.lorebook.repository.SettingBookCommandRepository;
import com.aivle.ai0917.ipai.domain.author.lorebook.repository.SettingBookViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettingBookServiceImpl implements SettingBookService {

    private final SettingBookViewRepository viewRepository;
    private final SettingBookCommandRepository commandRepository;

    @Override
    public Page<SettingBookResponseDto> getLorebookList(String userId, String title, Pageable pageable) {
        return viewRepository.findByUserIdAndTitle(userId, title, pageable)
                .map(SettingBookResponseDto::new);
    }

    @Override
    public SettingBookResponseDto getLorebookDetail(String id) {
        SettingBookView view = viewRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("설정 없음"));
        return new SettingBookResponseDto(view);
    }

    @Override
    public List<SettingBookResponseDto> getItemsByTag(String userId, String title, String tag) {
        return viewRepository.findByUserIdAndTitleAndTag(userId, title, tag)
                .stream()
                .map(SettingBookResponseDto::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void create(SettingBookCreateRequestDto request) {
        commandRepository.insert(
                request.getUserId(),
                request.getTitle(),
                request.getTag(),
                request.getKeyword(),
                request.getEpisode(),
                request.getSubtitle(),
                request.getSettings()
        );
    }

    @Override
    @Transactional
    public void update(UUID id, SettingBookUpdateRequestDto request) {
        if (commandRepository.update(
                id,
                request.getTitle(),
                request.getKeyword(),
                request.getSubtitle(),
                request.getSettings()
        ) == 0) {
            throw new RuntimeException("수정 대상 없음");
        }
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (commandRepository.delete(id) == 0) {
            throw new RuntimeException("삭제 대상 없음");
        }
    }
}
