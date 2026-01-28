package com.aivle.ai0917.ipai.domain.test.controller;


import com.aivle.ai0917.ipai.domain.test.service.AiHelloService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AiHelloController {

    private final AiHelloService aiHelloService;

    @GetMapping("/ai/hello")
    public String healthCheck() {
        return aiHelloService.checkAiServer();
    }
}
