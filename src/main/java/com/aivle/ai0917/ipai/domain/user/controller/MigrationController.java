//package com.aivle.ai0917.ipai.domain.user.controller;
//
//import com.aivle.ai0917.ipai.domain.user.service.UserMigrationService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequiredArgsConstructor
//public class MigrationController {
//    private final UserMigrationService migrationService;
//
//    @GetMapping("/api/v1/admin/access/migrate-users")
//    public String runMigration() {
//        migrationService.migrateUserIntegrationIds();
//        return "Migration complete! Check your logs.";
//    }
//}