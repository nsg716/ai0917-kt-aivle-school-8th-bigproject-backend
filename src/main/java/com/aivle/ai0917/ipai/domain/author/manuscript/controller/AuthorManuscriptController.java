// AuthorManuscriptController.java
package com.aivle.ai0917.ipai.domain.author.manuscript.controller;

import com.aivle.ai0917.ipai.domain.author.manuscript.dto.*;
import com.aivle.ai0917.ipai.domain.author.manuscript.service.ManuscriptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/author/{userId}/{title}/manuscript")
@RequiredArgsConstructor
public class AuthorManuscriptController {

    private final ManuscriptService manuscriptService;
    private final ObjectMapper objectMapper;

    @GetMapping("/list")
    public ResponseEntity<Page<ManuscriptResponseDto>> getManuscriptList(
            @PathVariable String userId,
            @PathVariable String title,
            @RequestParam(required = false) String keyword,
            @PageableDefault Pageable pageable) {

        return ResponseEntity.ok(
                manuscriptService.getManuscriptList(userId, title, keyword, pageable)
        );
    }

    @PostMapping("/upload")
    public ResponseEntity<UUID> uploadManuscript(
            @PathVariable String userId,
            @PathVariable String title,
            @RequestPart("data") String data,
            @RequestPart("file") MultipartFile file) throws IOException {

        ManuscriptRequestDto request = objectMapper.readValue(data, ManuscriptRequestDto.class);
        request.setUserId(userId);
        request.setTitle(title);

        return ResponseEntity.ok(manuscriptService.uploadManuscript(request, file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ManuscriptResponseDto> getManuscriptDetail(
            @PathVariable UUID id) {

        return ResponseEntity.ok(manuscriptService.getManuscriptDetail(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteManuscript(@PathVariable UUID id) {
        manuscriptService.deleteManuscript(id);
        return ResponseEntity.ok().build();
    }
}
