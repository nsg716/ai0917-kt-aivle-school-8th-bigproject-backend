package com.aivle.ai0917.ipai.domain.admin.access.dto;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Data
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class UserPageResponse {

    private final List<UserListResponseDto> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean last;

    public UserPageResponse(Page<UserListResponseDto> page) {
        this.content = page.getContent();
        this.page = page.getNumber();
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.last = page.isLast();
    }
}
