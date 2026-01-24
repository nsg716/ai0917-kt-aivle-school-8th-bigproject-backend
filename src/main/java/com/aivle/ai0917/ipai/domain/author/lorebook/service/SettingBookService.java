// SettingBookService.java
package com.aivle.ai0917.ipai.domain.author.lorebook.service;

import com.aivle.ai0917.ipai.domain.author.lorebook.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface SettingBookService {
    Page<SettingBookResponseDto> getLorebookList(String userId, String title, Pageable pageable);
    SettingBookResponseDto getLorebookDetail(String id);
    List<SettingBookResponseDto> getItemsByTag(String userId, String title, String tag);

    void create(SettingBookCreateRequestDto request);
    void update(UUID id, SettingBookUpdateRequestDto request);
    void delete(UUID id);
}
