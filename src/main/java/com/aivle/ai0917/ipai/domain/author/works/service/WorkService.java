//package com.aivle.ai0917.ipai.domain.author.works.service;
//
//import com.aivle.ai0917.ipai.domain.author.works.model.WorkStatus;
//import com.aivle.ai0917.ipai.domain.author.works.dto.WorkDto;
//import java.util.List;
//
//public interface WorkService {
//    List<WorkDto.Response> getAllWorks();
//    List<WorkDto.Response> getWorksByAuthorId(String userIntegrationId);
//    Long saveWork(WorkDto.CreateRequest requestDto);
//    Long updateWork(WorkDto.UpdateRequest requestDto);
//    // 1. 선택한 특정 작품 ID로 삭제하도록 변경
//    void deleteWork(Long id);
//
//    // 2. 상태값만 변경하는 로직 추가
//    Long updateWorkStatus(Long id, WorkStatus status);
//}