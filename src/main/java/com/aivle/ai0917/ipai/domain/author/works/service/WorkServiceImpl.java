package com.aivle.ai0917.ipai.domain.author.works.service;

import com.aivle.ai0917.ipai.domain.author.works.dto.WorkDto;
import com.aivle.ai0917.ipai.domain.author.works.model.Work;
import com.aivle.ai0917.ipai.domain.author.works.model.WorkStatus; // Import
import com.aivle.ai0917.ipai.domain.author.works.repository.WorkCommandRepository;
import com.aivle.ai0917.ipai.domain.author.works.repository.WorkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkServiceImpl implements WorkService {

    private final WorkRepository workRepository;
    private final WorkCommandRepository workCommandRepository;

    @Override
    public List<WorkDto.Response> getWorksByAuthor(String authorId, boolean sortByTitle) {
        // ... (이전과 동일, status 필터링 로직은 Repository에서 처리됨)
        List<Work> works;

        if (sortByTitle) {
            // Repository 메서드 이름이 길다면 줄일 수도 있지만, 기존 사용 유지
            works = workRepository.findAllByPrimaryAuthorIdAndStatusNotOrderByTitleAsc(authorId, WorkStatus.DELETED); // DELETED가 Enum에 있다면 사용, 없다면 Repository 쿼리 수정 필요 (아래 Repository 참고)
        } else {
            works = workRepository.findAllByPrimaryAuthorIdAndStatusNotOrderByCreatedAtDesc(authorId, WorkStatus.DELETED);
        }

        return works.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public WorkDto.Response getWorkDetail(Long id) {
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("작품이 존재하지 않습니다."));
        return convertToResponse(work);
    }

    @Override
    public Long saveWork(WorkDto.CreateRequest dto) {
        // [수정] 기본값으로 WorkStatus.ONGOING.name() 사용
        return workCommandRepository.insert(
                dto.getUniverseId(),
                dto.getPrimaryAuthorId(),
                dto.getTitle(),
                dto.getSynopsis(),
                dto.getGenre(),
                dto.getCoverImageUrl(),
                WorkStatus.NEW.name() // DB에 저장될 문자열 값
        );
    }

    @Override
    public void updateStatus(Long id, WorkStatus status) {
        // [수정] Enum.name()으로 문자열 변환하여 전달
        workCommandRepository.updateStatus(id, status.name());
    }

    @Override
    public void updateWork(Long id, WorkDto.UpdateRequest dto) {
        workCommandRepository.updateWork(
                id,
                dto.getTitle(),
                dto.getSynopsis(),
                dto.getGenre(),
                dto.getCoverImageUrl()
        );
    }

    @Override
    public void deleteWork(Long id) {
        workCommandRepository.deleteById(id);
    }

    private WorkDto.Response convertToResponse(Work view) {
        return WorkDto.Response.builder()
                .id(view.getId())
                .universeId(view.getUniverseId())
                .primaryAuthorId(view.getPrimaryAuthorId())
                .title(view.getTitle())
                .synopsis(view.getSynopsis())
                .genre(view.getGenre())
                .status(view.getStatus()) // Enum 타입 그대로 설정
                .statusDescription(view.getStatus().getDescription()) // 한글 설명 추가
                .coverImageUrl(view.getCoverImageUrl())
                .createdAt(view.getCreatedAt())
                .build();
    }
}