package com.aivle.ai0917.ipai.domain.author.works.controller;

import com.aivle.ai0917.ipai.domain.author.works.dto.WorkDto;
import com.aivle.ai0917.ipai.domain.author.works.model.WorkStatus; // Import
import com.aivle.ai0917.ipai.domain.author.works.service.WorkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/author/works")
@RequiredArgsConstructor
public class WorkController {

    private final WorkService workService;

    @GetMapping
    public ResponseEntity<List<WorkDto.Response>> getWorks(
            @RequestParam String authorId,
            @RequestParam(defaultValue = "true") boolean sortByTitle
    ) {
        return ResponseEntity.ok(workService.getWorksByAuthor(authorId, sortByTitle));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkDto.Response> getWorkDetail(@PathVariable Long id) {
        return ResponseEntity.ok(workService.getWorkDetail(id));
    }

    @PostMapping
    public ResponseEntity<Long> registerWork(@RequestBody WorkDto.CreateRequest request) {
        return ResponseEntity.ok(workService.saveWork(request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam WorkStatus status // [수정] String -> WorkStatus (자동 변환됨)
    ) {
        workService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    // ... (나머지는 동일)
}