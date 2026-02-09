package com.aivle.ai0917.ipai.domain.manager.ipext.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IpFileDownloadDto {
    private String filename; // 다운로드될 파일명 (예: 제목.pdf)
    private byte[] content;  // 파일 바이너리 데이터
}