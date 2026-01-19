package com.aivle.ai0917.ipai.domain.notice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeRequestDto {
    private String title;
    private String content;
    private String status;
    private String Writer;
    private String filePath;
}