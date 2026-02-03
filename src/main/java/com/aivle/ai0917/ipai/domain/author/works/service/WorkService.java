package com.aivle.ai0917.ipai.domain.author.works.service;

import com.aivle.ai0917.ipai.domain.author.works.dto.WorkDto;
import com.aivle.ai0917.ipai.domain.author.works.model.WorkStatus; // Import

import java.util.List;

public interface WorkService {
    List<WorkDto.Response> getWorksByAuthor(String authorId, boolean sortByTitle);
    WorkDto.Response getWorkDetail(Long id);
    Long saveWork(WorkDto.CreateRequest request);

    // [수정] String status -> WorkStatus status
    void updateStatus(Long id, WorkStatus status);

    void updateWork(Long id, WorkDto.UpdateRequest request);
    void deleteWork(Long id);
}