// ManuscriptServiceImpl.java
package com.aivle.ai0917.ipai.domain.author.manuscript.service;

import com.aivle.ai0917.ipai.domain.author.manuscript.dto.*;
import com.aivle.ai0917.ipai.domain.author.manuscript.model.ManuscriptView;
import com.aivle.ai0917.ipai.domain.author.manuscript.repository.ManuscriptCommandRepository;
import com.aivle.ai0917.ipai.domain.author.manuscript.repository.ManuscriptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManuscriptServiceImpl implements ManuscriptService {

    private final ManuscriptRepository manuscriptRepository;
    private final ManuscriptCommandRepository manuscriptCommandRepository;

    @Override
    public Page<ManuscriptResponseDto> getManuscriptList(
            String userId, String title, String keyword, Pageable pageable) {

        Page<ManuscriptView> page =
                (keyword == null || keyword.isBlank())
                        ? manuscriptRepository.findByUserIdAndTitle(userId, title, pageable)
                        : manuscriptRepository.findByUserIdAndTitleAndTitleContaining(
                        userId, title, keyword, pageable
                );

        return page.map(ManuscriptResponseDto::new);
    }

    @Override
    public ManuscriptResponseDto getManuscriptDetail(UUID id) {
        ManuscriptView manuscript = manuscriptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("원문을 찾을 수 없습니다."));
        return new ManuscriptResponseDto(manuscript);
    }

    @Override
    @Transactional
    public UUID uploadManuscript(ManuscriptRequestDto request, MultipartFile file) throws IOException {

        UUID id = UUID.randomUUID();
        String txt = new String(file.getBytes());

        manuscriptCommandRepository.insert(
                id,
                request.getUserId(),
                request.getTitle(),
                request.getEpisode(),
                request.getSubtitle(),
                txt
        );

        return id;
    }

    @Override
    @Transactional
    public void deleteManuscript(UUID id) {
        int deleted = manuscriptCommandRepository.deleteById(id);
        if (deleted == 0) {
            throw new RuntimeException("삭제할 원문이 없습니다.");
        }
    }
}
