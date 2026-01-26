package com.aivle.ai0917.ipai.domain.notice.service;

import com.aivle.ai0917.ipai.domain.notice.dto.NoticeRequestDto;
import com.aivle.ai0917.ipai.domain.notice.dto.NoticeResponseDto;
import com.aivle.ai0917.ipai.domain.notice.model.Notice;
import com.aivle.ai0917.ipai.domain.notice.repository.NoticeRepository;
import com.aivle.ai0917.ipai.global.utils.FileStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;
    private final FileStore fileStore;

    private static final String NOTICE_CATEGORY = "notices";

    @Override
    public Page<NoticeResponseDto> getNoticeList(String keyword, Pageable pageable) {
        // 1. 키워드가 없으면 전체 목록 조회
        if (keyword == null || keyword.isBlank()) {
            return noticeRepository.findAll(pageable).map(NoticeResponseDto::new);
        }
        else {
            // 2. 제목(Title)에 키워드가 포함된 데이터만 조회
            return noticeRepository.findByTitleContaining(keyword, pageable)
                    .map(NoticeResponseDto::new);
        }
    }

    @Override
    public NoticeResponseDto getNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항이 없습니다."));
        return new NoticeResponseDto(notice);
    }

    @Override
    @Transactional
    public Long createNotice(NoticeRequestDto request, MultipartFile file) throws IOException {
        Notice.NoticeBuilder builder = Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .writer(request.getWriter());

        // 파일이 있으면 저장
        if (file != null && !file.isEmpty()) {
            FileStore.FileInfo fileInfo = fileStore.storeFile(file, NOTICE_CATEGORY);
            if (fileInfo != null) {
                builder.filePath(fileInfo.getFilePath())
                        .originalFilename(fileInfo.getOriginalFilename())
                        .fileSize(fileInfo.getFileSize())
                        .contentType(fileInfo.getContentType());
            }
        }

        Notice notice = builder.build();
        return noticeRepository.save(notice).getId();
    }

    @Override
    @Transactional
    public void updateNotice(Long id, NoticeRequestDto request, MultipartFile file) throws IOException {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항이 없습니다."));

        // 새로운 파일이 업로드된 경우
        if (file != null && !file.isEmpty()) {
            // 기존 파일 삭제
            if (notice.hasFile()) {
                fileStore.deleteFile(notice.getFilePath());
            }

            // 새 파일 저장
            FileStore.FileInfo fileInfo = fileStore.storeFile(file, NOTICE_CATEGORY);
            if (fileInfo != null) {
                notice.updateFile(
                        fileInfo.getFilePath(),
                        fileInfo.getOriginalFilename(),
                        fileInfo.getFileSize(),
                        fileInfo.getContentType()
                );
            }
        }

        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setWriter(request.getWriter());
    }

    @Override
    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항이 없습니다."));

        // 파일이 있으면 삭제
        if (notice.hasFile()) {
            fileStore.deleteFile(notice.getFilePath());
        }

        noticeRepository.delete(notice);
    }

    @Override
    @Transactional
    public void deleteNoticeFile(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항이 없습니다."));

        // 파일이 존재하는 경우에만 삭제 처리
        if (notice.hasFile()) {
            // 1. 실제 물리 파일 삭제
            fileStore.deleteFile(notice.getFilePath());

            // 2. DB 정보 초기화 (엔티티 내 구현된 removeFile 메서드 호출)
            notice.removeFile();
        }
    }
}