package com.carvaluation.storage;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    private final Path rootLocation;

    public FileStorageService(StorageProperties properties) {
        this.rootLocation = properties.getRootLocation();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation.resolve("cars"));
            Files.createDirectories(rootLocation.resolve("temp"));
            Files.createDirectories(rootLocation.resolve("vin"));
            Files.createDirectories(rootLocation.resolve("odo"));
        } catch (IOException e) {
            log.error("Could not initialize storage directories: {}", e.getMessage());
        }
    }

    public String store(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Không thể lưu file rỗng");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            } else {
                extension = ".jpg";
            }

            // Chỉ cho phép định dạng ảnh hợp lệ
            if (!extension.matches("\\.(jpg|jpeg|png|webp|heic)$")) {
                throw new IllegalArgumentException("Định dạng file không được hỗ trợ (chỉ chấp nhận JPG, PNG, WEBP)");
            }

            String storedFileName = UUID.randomUUID().toString() + extension;
            Path targetDir = this.rootLocation.resolve(subDirectory != null ? subDirectory : "temp");
            Files.createDirectories(targetDir);

            Path destinationFile = targetDir.resolve(storedFileName).normalize().toAbsolutePath();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // Trả về relative path dạng "subDirectory/filename"
            return (subDirectory != null ? subDirectory + "/" : "") + storedFileName;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu trữ file ảnh: " + e.getMessage(), e);
        }
    }

    public byte[] loadAsBytes(String relativePath) {
        try {
            Path file = this.rootLocation.resolve(relativePath).normalize().toAbsolutePath();
            if (Files.exists(file) && Files.isReadable(file)) {
                return Files.readAllBytes(file);
            }
            throw new RuntimeException("Không tìm thấy file: " + relativePath);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi đọc file: " + e.getMessage(), e);
        }
    }

    public Path load(String relativePath) {
        return this.rootLocation.resolve(relativePath).normalize().toAbsolutePath();
    }

    public Path getRootLocation() {
        return rootLocation;
    }
}
