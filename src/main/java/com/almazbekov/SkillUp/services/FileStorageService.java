package com.almazbekov.SkillUp.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String storeFile(MultipartFile file, String subDirectory) throws IOException {
        // Создаем уникальное имя файла
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + fileExtension;

        // Создаем директорию, если она не существует
        Path uploadPath = Paths.get(uploadDir, subDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Сохраняем файл
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath);

        // Возвращаем относительный путь к файлу
        return subDirectory + "/" + newFilename;
    }

    public void deleteFile(String filePath) throws IOException {
        Path fullPath = Paths.get(uploadDir, filePath);
        if (Files.exists(fullPath)) {
            Files.delete(fullPath);
        }
    }
} 