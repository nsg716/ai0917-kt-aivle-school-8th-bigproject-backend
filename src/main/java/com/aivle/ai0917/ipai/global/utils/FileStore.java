package com.aivle.ai0917.ipai.global.utils;

import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class FileStore {

    @Value("${file.dir:./uploads/}")
    private String fileDir;

    public String getFullPath(String filename) {
        return fileDir + filename;
    }

    /**
     * 파일 저장 (기본) - 기존 코드 호환성 유지
     */
    public String storeFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) return null;

        String originalFilename = multipartFile.getOriginalFilename();
        String storeFileName = createStoreFileName(originalFilename);

        File dir = new File(fileDir);
        if (!dir.exists()) dir.mkdirs();

        multipartFile.transferTo(new File(getFullPath(storeFileName)));
        return storeFileName;
    }

    /**
     * 파일 저장 (카테고리별 날짜 폴더 구조) - 개선 버전
     */
    public FileInfo storeFile(MultipartFile multipartFile, String category) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String storeFileName = createStoreFileName(originalFilename);
        String datePath = createDatePath(category);

        File dir = new File(fileDir + datePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fullPath = datePath + storeFileName;
        multipartFile.transferTo(new File(getFullPath(fullPath)));

        return FileInfo.builder()
                .filePath(fullPath)
                .originalFilename(originalFilename)
                .fileSize(multipartFile.getSize())
                .contentType(multipartFile.getContentType())
                .build();
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String storeFileName) {
        if (storeFileName == null) return;
        File file = new File(getFullPath(storeFileName));
        if (file.exists()) file.delete();
    }

    /**
     * UUID 기반 파일명 생성
     */
    private String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    /**
     * 확장자 추출
     */
    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }

    /**
     * 날짜별 경로 생성 (예: notices/2026/01/)
     */
    private String createDatePath(String category) {
        LocalDate now = LocalDate.now();
        return String.format("%s/%d/%02d/",
                category, now.getYear(), now.getMonthValue());
    }

    /**
     * 파일 정보 DTO
     */
    @Getter
    @Builder
    public static class FileInfo {
        private String filePath;
        private String originalFilename;
        private Long fileSize;
        private String contentType;
    }
}