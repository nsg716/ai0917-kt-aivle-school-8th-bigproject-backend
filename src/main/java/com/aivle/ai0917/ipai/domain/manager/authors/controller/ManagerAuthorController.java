package com.aivle.ai0917.ipai.domain.manager.authors.controller;

import com.aivle.ai0917.ipai.domain.manager.authors.service.ManagerAuthorService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/manager")
public class ManagerAuthorController {

    private final ManagerAuthorService managerAuthorService;

    public ManagerAuthorController(ManagerAuthorService managerAuthorService) {
        this.managerAuthorService = managerAuthorService;
    }

    // POST /api/v1/manager/author/{pwd}
    @PostMapping("/author/{pwd}")
    public Map<String, Object> matchAuthor(@PathVariable("pwd") String pwd,
                                           Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new RuntimeException("인증 정보가 없습니다.");
        }
        return managerAuthorService.matchAuthorByInviteCode(userId, pwd);
    }
}
