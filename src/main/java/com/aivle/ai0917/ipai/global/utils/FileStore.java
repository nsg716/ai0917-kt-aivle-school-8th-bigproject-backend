package com.aivle.ai0917.ipai.global.utils;

import jakarta.annotation.PostConstruct;
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

    @Value("${file.dir:./uploads/}") // 기본값 설정
    private String fileDir;

    private String absolutePath;

    @PostConstruct
    public void init() {
        // 1. 설정값으로부터 절대 경로 추출
        File dir = new File(fileDir);
        this.absolutePath = dir.getAbsolutePath();

        // 2. 경로 끝에 OS별 구분자(\ 또는 /) 추가 확인
        if (!this.absolutePath.endsWith(File.separator)) {
            this.absolutePath += File.separator;
        }

        // 3. 디렉토리가 없으면 생성
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // 핵심: 이제 모든 곳에서 'absolutePath'를 사용해야 합니다!
    public String getFullPath(String filename) {
        return absolutePath + filename;
    }

    /**
     * 파일 저장 (기본)
     */
    public String storeFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) return null;

        String originalFilename = multipartFile.getOriginalFilename();
        String storeFileName = createStoreFileName(originalFilename);

        // getFullPath가 이제 절대 경로를 반환하므로 안전합니다.
        multipartFile.transferTo(new File(getFullPath(storeFileName)));
        return storeFileName;
    }

    /**
     * 파일 저장 (카테고리별 날짜 폴더 구조)
     */
    public FileInfo storeFile(MultipartFile multipartFile, String category) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String storeFileName = createStoreFileName(originalFilename);
        String datePath = createDatePath(category);

        // 절대 경로 + 날짜 경로를 합친 디렉토리 생성
        File dir = new File(absolutePath + datePath);
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

    private String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return (pos == -1) ? "" : originalFilename.substring(pos + 1);
    }

    /**
     * 날짜별 경로 생성 (OS 호환성 위해 File.separator 사용)
     */
    private String createDatePath(String category) {
        LocalDate now = LocalDate.now();
        // %s/%d/%02d/ 대신 File.separator를 사용하여 OS 독립성 확보
        return String.format("%s%s%d%s%02d%s",
                category, File.separator,
                now.getYear(), File.separator,
                now.getMonthValue(), File.separator);
    }

    @Getter
    @Builder
    public static class FileInfo {
        private String filePath;
        private String originalFilename;
        private Long fileSize;
        private String contentType;
    }
}