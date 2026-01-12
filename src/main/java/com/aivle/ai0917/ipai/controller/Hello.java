package com.aivle.ai0917.ipai.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class Hello {

    @GetMapping("/hello")
    public String helloUser() {
        return "helloUser AIVLE SCHOOL 8th";
    }
}
