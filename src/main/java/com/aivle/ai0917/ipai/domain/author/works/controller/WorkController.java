//package com.aivle.ai0917.ipai.domain.author.works.controller;
//
//import com.aivle.ai0917.ipai.domain.author.works.model.WorkStatus;
//import com.aivle.ai0917.ipai.domain.author.works.dto.WorkDto;
//import com.aivle.ai0917.ipai.domain.author.works.service.WorkService;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/v1/author/works")
//@RequiredArgsConstructor
//public class WorkController {
//
//    private final WorkService workService;
//
//    // 1. 작품 관리 목록 조회
//    @GetMapping
//    public ResponseEntity<List<WorkDto.Response>> getAllWorks() {
//        return ResponseEntity.ok(workService.getAllWorks());
//    }
//
//    // 2. 작품 관리 상세 조회
//    @GetMapping("/{IntegrationId}")
//    public ResponseEntity<List<WorkDto.Response>> getWork(@PathVariable("IntegrationId") String ipid) {
//        return ResponseEntity.ok(workService.getWorksByAuthorId(ipid));
//    }
//
//    // 3. 작품 등록
//    @PostMapping
//    public ResponseEntity<Long> registerWork(@RequestBody WorkDto.CreateRequest requestDto) {
//        return ResponseEntity.ok(workService.saveWork(requestDto));
//    }
//
//    // 4. 작품 수정 및 상태 변경
//    @PatchMapping
//    public ResponseEntity<Long> updateWork(@RequestBody WorkDto.UpdateRequest requestDto) {
//        return ResponseEntity.ok(workService.updateWork(requestDto));
//    }
//
//    @PatchMapping("/{id}/status")
//    public ResponseEntity<Long> updateStatus(
//            @PathVariable("id") Long id,
//            @RequestParam("status") WorkStatus status) {
//        return ResponseEntity.ok(workService.updateWorkStatus(id, status));
//    }
//    // 5. 작품 삭제
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteWork(@PathVariable("id") Long id) {
//        workService.deleteWork(id);
//        return ResponseEntity.noContent().build();
//    }
//}