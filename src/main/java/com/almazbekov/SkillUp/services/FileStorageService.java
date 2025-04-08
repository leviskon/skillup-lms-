package com.almazbekov.SkillUp.services;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {
    private final Path fileStorageLocation;

    public FileStorageService() {
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("Создана директория для загрузки файлов: {}", this.fileStorageLocation);
        } catch (IOException ex) {
            log.error("Ошибка при создании директории для загрузки файлов", ex);
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file, String subDirectory) throws IOException {
        log.info("Сохранение файла в директорию: {}", subDirectory);
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path targetLocation = this.fileStorageLocation.resolve(subDirectory).resolve(fileName);
        Files.createDirectories(targetLocation.getParent());
        Files.copy(file.getInputStream(), targetLocation);
        log.info("Файл успешно сохранен: {}", targetLocation);
        return subDirectory + "/" + fileName;
    }

    public void deleteFile(String fileUrl) throws IOException {
        log.info("Удаление файла: {}", fileUrl);
        Path filePath = this.fileStorageLocation.resolve(fileUrl).normalize();
        Files.deleteIfExists(filePath);
        log.info("Файл успешно удален: {}", filePath);
    }

    public Resource loadFileAsResource(String fileUrl) throws IOException {
        try {
            log.info("Загрузка файла: {}", fileUrl);
            Path filePath = this.fileStorageLocation.resolve(fileUrl).normalize();
            
            if (!Files.exists(filePath)) {
                log.error("Файл не найден: {}", filePath);
                throw new RuntimeException("File not found: " + fileUrl);
            }
            
            if (!Files.isReadable(filePath)) {
                log.error("Файл недоступен для чтения: {}", filePath);
                throw new RuntimeException("File not readable: " + fileUrl);
            }
            
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                log.info("Файл успешно загружен: {}", filePath);
                return resource;
            } else {
                log.error("Файл не найден или недоступен для чтения: {}", fileUrl);
                throw new RuntimeException("File not found or not readable: " + fileUrl);
            }
        } catch (MalformedURLException ex) {
            log.error("Ошибка при загрузке файла: {}", fileUrl, ex);
            throw new RuntimeException("File not found: " + fileUrl, ex);
        }
    }
} 