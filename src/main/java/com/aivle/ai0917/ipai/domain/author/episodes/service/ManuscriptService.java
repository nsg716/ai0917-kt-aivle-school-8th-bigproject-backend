package com.aivle.ai0917.ipai.domain.author.episodes.service;

import com.aivle.ai0917.ipai.domain.author.episodes.client.AiAnalysisClient;
import com.aivle.ai0917.ipai.domain.author.episodes.dto.CategoryAnalysisRequestDto;
import com.aivle.ai0917.ipai.domain.author.episodes.dto.ManuscriptRequestDto;
import com.aivle.ai0917.ipai.domain.author.episodes.dto.ManuscriptResponseDto;
import com.aivle.ai0917.ipai.domain.author.episodes.dto.ManuscriptUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ManuscriptService {

    Page<ManuscriptResponseDto> getManuscriptList(
            String userId, String title, String keyword, Pageable pageable
    );

    ManuscriptResponseDto getManuscriptDetail(Long id);

//    Long uploadManuscript(ManuscriptRequestDto request);

    // [변경] 기존 uploadManuscript -> create / modify 분리
    Long createManuscript(ManuscriptRequestDto request);

    Long modifyManuscriptText(ManuscriptRequestDto request);

    // [수정] 개별 파라미터 대신 DTO 사용
    AiAnalysisClient.CategoryExtractionResponse extractCategories(
            String userId, CategoryAnalysisRequestDto requestDto
    );

    AiAnalysisClient.SettingConflictResponse checkSettingConflict(
            Long workId, String userId, AiAnalysisClient.CategoryExtractionResponse categories
    );

    void deleteManuscript(Long id);

    void updateManuscript(Long id, ManuscriptUpdateRequestDto request);
}