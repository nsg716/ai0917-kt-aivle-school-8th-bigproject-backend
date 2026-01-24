// ManuscriptService.java
package com.aivle.ai0917.ipai.domain.author.manuscript.service;

import com.aivle.ai0917.ipai.domain.author.manuscript.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface ManuscriptService {

    Page<ManuscriptResponseDto> getManuscriptList(
            String userId, String title, String keyword, Pageable pageable
    );

    ManuscriptResponseDto getManuscriptDetail(UUID id);

    UUID uploadManuscript(ManuscriptRequestDto request, MultipartFile file) throws IOException;

    void deleteManuscript(UUID id);
}
