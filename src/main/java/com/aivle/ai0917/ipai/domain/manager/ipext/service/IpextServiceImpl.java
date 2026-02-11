package com.aivle.ai0917.ipai.domain.manager.ipext.service;

import com.aivle.ai0917.ipai.domain.admin.access.model.UserRole;
import com.aivle.ai0917.ipai.domain.author.lorebook.model.SettingBookView;
import com.aivle.ai0917.ipai.domain.author.lorebook.repository.SettingBookViewRepository;
import com.aivle.ai0917.ipai.domain.author.works.model.Work;
import com.aivle.ai0917.ipai.domain.author.works.model.WorkStatus;
import com.aivle.ai0917.ipai.domain.author.works.repository.WorkCommandRepository;
import com.aivle.ai0917.ipai.domain.author.works.repository.WorkRepository;
import com.aivle.ai0917.ipai.domain.manager.authors.repository.ManagerAuthorRepository;
import com.aivle.ai0917.ipai.domain.manager.info.dto.ManagerNoticeDto; // [추가]
import com.aivle.ai0917.ipai.domain.manager.info.service.ManagerNoticeService;
import com.aivle.ai0917.ipai.domain.manager.ipext.client.AiIpExtClient;
import com.aivle.ai0917.ipai.domain.manager.ipext.dto.*;
import com.aivle.ai0917.ipai.domain.manager.ipext.model.IpProposal;
import com.aivle.ai0917.ipai.domain.manager.ipext.repository.IpProposalRepository;
import com.aivle.ai0917.ipai.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException; // [수정] 표준 예외 사용
import java.util.stream.Collectors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IpextServiceImpl implements IpextService {

    private final IpProposalRepository ipProposalRepository;
    private final ManagerAuthorRepository managerAuthorRepository;
    private final WorkRepository workRepository;
    private final WorkCommandRepository workCommandRepository;
    private final SettingBookViewRepository settingBookViewRepository;
    private final AiIpExtClient aiIpExtClient;
    private final IpProposalSaveService ipProposalSaveService;
    private final ManagerNoticeService managerNoticeService;

    // 1. IP 확장 조회 (목록) - ManagerId 기준
    @Override
    public Page<IpProposalResponseDto> getProposalList(String managerId, Pageable pageable) {
        return ipProposalRepository.findAllActiveByManagerId(managerId, pageable)
                .map(IpProposalResponseDto::new);
    }

    // 2. IP 확장 제안 상세 조회 - ManagerId & ID 기준
    @Override
    public IpProposalResponseDto getProposalDetail(String managerId, Long id) {
        IpProposal proposal = ipProposalRepository.findActiveByIdAndManagerId(id, managerId)
                .orElseThrow(() -> new NoSuchElementException("해당 제안서를 찾을 수 없거나 접근 권한이 없습니다. ID: " + id));

        return new IpProposalResponseDto(proposal);
    }

    // 3. IP 확장 제안 수정 - ManagerId & ID 기준
    @Override
    @Transactional
    public void updateProposal(String managerId, Long id, IpProposalRequestDto request) {
        IpProposal proposal = ipProposalRepository.findActiveByIdAndManagerId(id, managerId)
                .orElseThrow(() -> new NoSuchElementException("수정할 제안서를 찾을 수 없거나 접근 권한이 없습니다. ID: " + id));

        // 엔티티 업데이트 로직
        proposal.updateFromRequest(
                request.getTitle(),
                request.getTargetFormat(),
                request.getTargetGenre(),
                request.getWorldSetting(),
                request.getTargetAges(),
                request.getTargetGender(),
                request.getBudgetScale(),
                request.getToneAndManner(),
                request.getMediaDetail(),
                request.getAddPrompt()
        );
    }

    // 4. IP 확장 제안 삭제
    @Override
    @Transactional
    public void deleteProposal(Long id) {
        IpProposal proposal = ipProposalRepository.findActiveById(id)
                .orElseThrow(() -> new NoSuchElementException("삭제할 제안서를 찾을 수 없습니다. ID: " + id));

        // Soft Delete 처리
        proposal.softDelete();
    }

    // 5. IP 확장 제안 기획서 미리보기
    @Override
    public IpProposalResponseDto getProposalPreview(Long id) {
        // 미리보기는 단순 ID 조회 (필요시 managerId 추가 가능)
        IpProposal proposal = ipProposalRepository.findActiveById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 제안서를 찾을 수 없습니다. ID: " + id));
        return new IpProposalResponseDto(proposal);
    }

    @Override
    public List<AuthorMatchResponseDto> getMatchedAuthors(String managerId) {
        // managerId(String)는 User 엔티티의 managerIntegrationId와 매칭됩니다.
        // 페이징 없이 전체 목록을 가져오거나, 필요하다면 Pageable을 추가해야 합니다.
        // 여기서는 편의상 전체 목록(Unpaged)으로 가져오는 예시입니다.

        Page<User> authorsPage = managerAuthorRepository.findMatchedAuthors(
                managerId,
                null, // keyword 없음
                UserRole.Author,
                Pageable.unpaged()
        );

        return authorsPage.stream()
                .map(author -> {
                    // 각 작가의 작품 수 조회 (DELETED 제외)
                    long workCount = workRepository.countByPrimaryAuthorIdAndStatusNot(
                            author.getIntegrationId(),
                            WorkStatus.DELETED
                    );

                    return AuthorMatchResponseDto.builder()
                            .id(author.getId())
                            .name(author.getName())
                            .email(author.getSiteEmail())
                            .workCount(workCount)
                            .integrationId(author.getIntegrationId())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<AuthorWorkDto> getAuthorWorks(String managerId) {
        List<Work> works = workCommandRepository.findAllByManagerId(managerId, WorkStatus.DELETED);

        return works.stream()
                .map(work -> AuthorWorkDto.builder()
                        .workId(work.getId())
                        .title(work.getTitle())
                        .primaryAuthorId(work.getPrimaryAuthorId())
                        .genre(work.getGenre())
                        .status(work.getStatus().name())
                        .coverImageUrl(work.getCoverImageUrl())
                        .createdAt(work.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchedLorebookDto> getWorkLorebooks(Long workId) {

        // 1. 작품 정보 조회 (제목을 가져오기 위함 + 존재 여부 확인)
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new NoSuchElementException("해당 작품을 찾을 수 없습니다. ID: " + workId));

        // 2. 해당 작품의 설정집 목록 조회
        List<SettingBookView> lorebooks = settingBookViewRepository.findAllByWorkId(workId);

        // 3. DTO 변환 (하나의 작품에 대한 설정집들이므로 workTitle은 동일합니다)
        return lorebooks.stream()
                .map(lb -> MatchedLorebookDto.builder()
                        .lorebookId(lb.getId())
                        .keyword(lb.getKeyword())
                        .category(lb.getCategory())
                        .description(lb.getSetting() != null ? lb.getSetting().toString() : "")
                        .workId(work.getId())
                        .workTitle(work.getTitle())
                        .build())
                .collect(Collectors.toList());
    }

    // [2단계] 설정집 충돌 여부 확인 (AI)
    @Override
    public AiIpExtClient.LorebookCheckResponse checkSettingsConflict(ConflictCheckRequestDto request) {
        // request.getLorebooks()는 프론트에서 받은 원본 로어북 리스트 (List<Map<String, Object>>)
        if (request.getLorebooks() == null || request.getLorebooks().isEmpty()) {
            throw new IllegalArgumentException("충돌 검사를 수행할 설정집(Lorebook) 데이터가 없습니다.");
        }

        log.info("AI 설정 충돌 검사 실행. Lorebooks count: {}", request.getLorebooks().size());

        // AI 서버 호출 -> 결과 반환 (이 결과에 processed_lorebooks가 포함됨)
        return aiIpExtClient.checkLorebookConflict(request.getLorebooks());
    }

    @Override
    @Transactional
    public AiIpExtClient.ProposalResponse createProposal(IpProposalRequestDto request) {
        if (request.getManagerId() == null) {
            throw new IllegalArgumentException("Manager ID는 필수입니다.");
        }

        // 1. 별도 트랜잭션으로 DB 저장 (즉시 커밋됨)
        Long proposalId = ipProposalSaveService.saveProposal(request);

        // 2. AI 서버 호출 (이미 DB에 커밋된 상태)
        log.info("AI 서버로 IP 기획서 PDF 생성 요청 시작. Proposal ID={}", proposalId);

        AiIpExtClient.ProposalResponse response = aiIpExtClient.createIpProposal(
                proposalId,
                request.getProcessedLorebooks()
        );

        managerNoticeService.sendNotice(
                request.getManagerId(), // 수신자: 매니저
                ManagerNoticeDto.ManagerNoticeSource.IP_EXT,
                "IP 확장 제안서 생성 완료",
                "'" + request.getTitle() + "' 제안서(PDF) 생성이 완료되었습니다.",
                "/manager/ipext/" + proposalId // 클릭 시 제안서 상세/다운로드 페이지
        );

        return response;
    }

    // [추가] IP 확장 제안서 다운로드 구현
    @Override
    public IpFileDownloadDto downloadProposal(Long id) {
        // 1. 제안서 조회 (Status가 DELETED가 아닌 것)
        IpProposal proposal = ipProposalRepository.findActiveById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 제안서를 찾을 수 없습니다. ID: " + id));

        // 2. 파일 경로 확인
        String filePath = proposal.getFilePath();
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalStateException("생성된 PDF 파일 경로가 존재하지 않습니다. (아직 생성 중이거나 실패했을 수 있습니다.)");
        }

        // 3. 실제 파일 존재 여부 확인 및 읽기
        try {
            Path path = Paths.get(filePath);
            File file = path.toFile();

            if (!file.exists()) {
                throw new IllegalStateException("서버 디스크에서 파일을 찾을 수 없습니다. 경로: " + filePath);
            }

            byte[] content = Files.readAllBytes(path);

            // 4. 다운로드 파일명 생성 (제안서 제목 + .pdf)
            // 공백을 언더바(_)로 치환하거나 안전한 파일명으로 변경
            String downloadFilename = proposal.getTitle().replaceAll("\\s+", "_") + ".pdf";

            return IpFileDownloadDto.builder()
                    .filename(downloadFilename)
                    .content(content)
                    .build();

        } catch (IOException e) {
            log.error("파일 읽기 실패: filePath={}", filePath, e);
            throw new RuntimeException("파일을 읽어오는 중 오류가 발생했습니다.", e);
        }
    }
}