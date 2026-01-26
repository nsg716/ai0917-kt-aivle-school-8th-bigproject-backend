// AuthorLorebookController.java
package com.aivle.ai0917.ipai.domain.author.lorebook.controller;

import com.aivle.ai0917.ipai.domain.author.lorebook.dto.*;
import com.aivle.ai0917.ipai.domain.author.lorebook.model.LorebookTag;
import com.aivle.ai0917.ipai.domain.author.lorebook.service.SettingBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/author/{userId}/{title}/lorebook")
@RequiredArgsConstructor
public class AuthorLorebookController {

    private final SettingBookService lorebookService;

    @GetMapping
    public ResponseEntity<Page<SettingBookResponseDto>> getLorebookMain(
            @PathVariable String userId,
            @PathVariable String title,
            Pageable pageable
    ) {
        return ResponseEntity.ok(lorebookService.getLorebookList(userId, title, pageable));
    }

    @GetMapping("/{tags}")
    public ResponseEntity<List<SettingBookResponseDto>> getByTag(
            @PathVariable String userId,
            @PathVariable String title,
            @PathVariable String tags
    ) {
        LorebookTag tag = LorebookTag.fromPath(tags);
        return ResponseEntity.ok(
                lorebookService.getItemsByTag(userId, title, tag.getDescription())
        );
    }

    @GetMapping("/{tags}/{itemId}")
    public ResponseEntity<SettingBookResponseDto> getDetail(
            @PathVariable String itemId
    ) {
        return ResponseEntity.ok(lorebookService.getLorebookDetail(itemId));
    }

    @PostMapping("/{tags}")
    public ResponseEntity<Void> create(
            @PathVariable String userId,
            @PathVariable String title,
            @PathVariable String tags,
            @RequestBody SettingBookCreateRequestDto request
    ) {
        request.setTag(LorebookTag.fromPath(tags).getDescription());
        request.setTitle(title);
        lorebookService.create(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{tags}/{itemId}")
    public ResponseEntity<Void> update(
            @PathVariable UUID itemId,
            @RequestBody SettingBookUpdateRequestDto request
    ) {
        lorebookService.update(itemId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{tags}/{itemId}")
    public ResponseEntity<Void> delete(@PathVariable UUID itemId) {
        lorebookService.delete(itemId);
        return ResponseEntity.ok().build();
    }
}
