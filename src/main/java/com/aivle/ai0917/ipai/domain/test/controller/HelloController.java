package com.aivle.ai0917.ipai.domain.test.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/v1")
public class HelloController {

    @GetMapping("/hello")
    public String helloUser() {
        return "helloUser AIVLE SCHOOL 8th";
    }

    @GetMapping("/auth/naver/hello")
    public String naverHelloUser() {
        return "naverhelloUser AIVLE SCHOOL 8th";
    }
}
