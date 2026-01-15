package com.aivle.ai0917.ipai.domain.test.controller;

import com.aivle.ai0917.ipai.domain.test.model.Test;
import com.aivle.ai0917.ipai.domain.test.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor // Repository를 자동으로 주입(Injection)해줍니다.
public class TestController {

    private final TestRepository testRepository;

    @GetMapping("/api/test")
    public List<Test> getAllData() {
        // DB에 있는 모든 데이터를 조회해서 JSON 형태로 반환합니다.
        return testRepository.findAll();
    }
}