package com.aivle.ai0917.ipai.domain.notice.controller;

import com.aivle.ai0917.ipai.domain.notice.dto.NoticeRequestDto;
import com.aivle.ai0917.ipai.domain.notice.dto.NoticeResponseDto;
import com.aivle.ai0917.ipai.domain.notice.service.NoticeService;
import com.aivle.ai0917.ipai.global.utils.FileStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/v1/admin/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final ObjectMapper objectMapper;
    private final FileStore fileStore;

    /**
     * 공지사항 목록 조회
     * @param keyword
     * @param pageable
     * @return
     */
    @GetMapping
    public ResponseEntity<Page<NoticeResponseDto>> getNoticeList(
            @RequestParam(required = false) String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(noticeService.getNoticeList(keyword, pageable));
    }

    /**
     * 공지사항 상세 조회
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<NoticeResponseDto> getNotice(@PathVariable Long id) {
        return ResponseEntity.ok(noticeService.getNotice(id));
    }

    /**
     * 새 공지사항 작성
     * @param data
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping
    public ResponseEntity<Long> createNotice(
            @RequestPart("data") String data,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        NoticeRequestDto request = objectMapper.readValue(data, NoticeRequestDto.class);
        return ResponseEntity.ok(noticeService.createNotice(request, file));
    }

    /**
     * 공지사항 정보 수정
     * @param id
     * @param data
     * @param file
     * @return
     * @throws IOException
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateNotice(
            @PathVariable Long id,
            @RequestPart("data") String data,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        NoticeRequestDto request = objectMapper.readValue(data, NoticeRequestDto.class);
        noticeService.updateNotice(id, request, file);
        return ResponseEntity.ok().build();
    }

    /**
     * 공지사항 삭제
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 파일 다운로드
     * @param id
     * @return
     * @throws MalformedURLException
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) throws MalformedURLException {
        NoticeResponseDto notice = noticeService.getNotice(id);

        // 파일이 없는 경우
        if (notice.getFilePath() == null || notice.getFilePath().isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 파일 경로에서 Resource 생성
        Path filePath = Paths.get(fileStore.getFullPath(notice.getFilePath()));
        Resource resource = new UrlResource(filePath.toUri());

        // 파일이 존재하지 않는 경우
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        // 원본 파일명 인코딩 (한글 파일명 지원)
        String encodedFilename = java.net.URLEncoder.encode(
                        notice.getOriginalFilename(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        // Content-Type 설정 (파일 타입에 따라)
        String contentType = notice.getContentType();
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFilename + "\"")
                .body(resource);
    }
    /**
     * 공지사항 첨부파일 삭제
     * @param id
     * @return
     */
    @DeleteMapping("/{id}/file")
    public ResponseEntity<Void> deleteNoticeFile(@PathVariable Long id) {
        noticeService.deleteNoticeFile(id);
        return ResponseEntity.ok().build();
    }
}