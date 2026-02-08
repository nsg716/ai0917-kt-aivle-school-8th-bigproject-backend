package com.aivle.ai0917.ipai.domain.manager.ipext.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthorMatchResponseDto {
    private Long id;
    private String name;
    private String email;
    private String integrationId;
    private long workCount;
}