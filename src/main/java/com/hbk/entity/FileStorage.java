package com.hbk.entity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.UUID;

@Component
public class FileStorage {

    private final Path root;

    public FileStorage(@Value("${app.upload-dir}") String uploadDir) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public StoredFile save(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("이미지 파일이 비었습니다.");

        Files.createDirectories(root);

        String original = StringUtils.cleanPath(file.getOriginalFilename());
        String ext = "";
        int dot = original.lastIndexOf(".");
        if (dot >= 0) ext = original.substring(dot);

        String savedName = UUID.randomUUID() + ext;
        Path target = root.resolve(savedName);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // imageUrl은 "정적 리소스 경로"로 열리게 만들 것
        String imageUrl = "/uploads/" + savedName;

        return new StoredFile(savedName, target.toString(), imageUrl);
    }

    public void deleteByPath(String path) {
        try {
            if (path == null || path.isBlank()) return;
            Files.deleteIfExists(Paths.get(path));
        } catch (Exception ignored) {}
    }

    public record StoredFile(String fileName, String filePath, String url) {}
}
