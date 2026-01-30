//package com.aivle.ai0917.ipai.domain.author.works.service;
//
//import com.aivle.ai0917.ipai.domain.author.works.model.WorkStatus;
//import com.aivle.ai0917.ipai.domain.author.works.dto.WorkDto;
//import com.aivle.ai0917.ipai.domain.author.works.model.Work;
//import com.aivle.ai0917.ipai.domain.author.works.repository.WorkRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class WorkServiceImpl implements WorkService {
//
//    private final WorkRepository workRepository;
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<WorkDto.Response> getAllWorks() {
//        return workRepository.findAll().stream()
//                .map(WorkDto.Response::from)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<WorkDto.Response> getWorksByAuthorId(String userIntegrationId) {
//        List<Work> works = workRepository.findByUserIntegrationId(userIntegrationId);
//        return works.stream()
//                .map(WorkDto.Response::from)
//                .collect(Collectors.toList());
//    }
//
//
//
//
//    @Override
//    @Transactional
//    public Long saveWork(WorkDto.CreateRequest requestDto) {
//        return workRepository.save(requestDto.toEntity()).getId();
//    }
//
//    @Override
//    @Transactional
//    public Long updateWork(WorkDto.UpdateRequest requestDto) {
//        // 수정 시에는 내부 식별자(Long id)를 사용하는 것이 정확합니다.
//        Work work = workRepository.findById(requestDto.getId())
//                .orElseThrow(() -> new IllegalArgumentException("해당 작품이 없습니다. id=" + requestDto.getId()));
//
//        work.update(requestDto.getTitle(), requestDto.getDescription(), requestDto.getStatus());
//        return work.getId();
//    }
//
//    @Override
//    @Transactional
//    public Long updateWorkStatus(Long id, WorkStatus status) {
//        Work work = workRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("해당 작품이 없습니다. id=" + id));
//
//        // 상태값만 변경 (엔티티의 update 메서드 활용)
//        work.update(null, null, status);
//        return work.getId();
//    }
//    @Override
//    @Transactional
//    public void deleteWork(Long id) {
//        // 작가 ID가 아닌 작품의 고유 PK(id)로 삭제
//        Work work = workRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("해당 작품이 없습니다. id=" + id));
//        workRepository.delete(work);
//    }
//
//}