package com.almazbekov.SkillUp.controllers;

import com.almazbekov.SkillUp.DTO.MaterialCreateDTO;
import com.almazbekov.SkillUp.entity.Material;
import com.almazbekov.SkillUp.services.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;
    private static final Logger log = LoggerFactory.getLogger(MaterialController.class);
    private final ObjectMapper objectMapper;

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Material>> getMaterialsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(materialService.getMaterialsByCourse(courseId));
    }

    @GetMapping("/{materialId}")
    public ResponseEntity<Material> getMaterialById(@PathVariable Long materialId) {
        return ResponseEntity.ok(materialService.getMaterialById(materialId));
    }

    @GetMapping("/course/{courseId}/type/{typeId}")
    public ResponseEntity<List<Material>> getMaterialsByCourseAndType(
            @PathVariable Long courseId,
            @PathVariable Long typeId) {
        return ResponseEntity.ok(materialService.getMaterialsByCourseAndType(courseId, typeId));
    }

    @PostMapping
    public ResponseEntity<Material> createMaterial(
            @ModelAttribute MaterialCreateDTO materialDTO) throws IOException {
        return ResponseEntity.ok(materialService.createMaterial(materialDTO));
    }

    @PutMapping("/{materialId}")
    public ResponseEntity<Material> updateMaterial(
            @PathVariable Long materialId,
            @ModelAttribute MaterialCreateDTO materialDTO) throws IOException {
        return ResponseEntity.ok(materialService.updateMaterial(materialId, materialDTO));
    }

    @DeleteMapping("/{materialId}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long materialId) throws IOException {
        materialService.deleteMaterial(materialId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{materialId}/file/{fileIndex}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Resource> getMaterialFile(
            @PathVariable Long materialId,
            @PathVariable int fileIndex) throws IOException {
        log.info("Получение файла {} для материала с ID: {}", fileIndex, materialId);
        
        Material material = materialService.getMaterialById(materialId);
        if (material == null) {
            log.error("Материал с ID {} не найден", materialId);
            return ResponseEntity.notFound().build();
        }
        
        List<String> urls;
        try {
            urls = objectMapper.readValue(material.getUrl(), List.class);
        } catch (Exception e) {
            log.error("Ошибка при чтении URL файлов: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
        
        if (urls == null || urls.isEmpty()) {
            log.error("Нет файлов для материала с ID {}", materialId);
            return ResponseEntity.notFound().build();
        }
        
        if (fileIndex < 0 || fileIndex >= urls.size()) {
            log.error("Неверный индекс файла: {} для материала с ID {}", fileIndex, materialId);
            return ResponseEntity.badRequest().build();
        }
        
        Resource file = materialService.getMaterialFile(materialId, fileIndex);
        if (file == null || !file.exists()) {
            log.error("Файл не найден для материала с ID {} и индексом {}", materialId, fileIndex);
            return ResponseEntity.notFound().build();
        }
        
        String contentType;
        try {
            contentType = Files.probeContentType(file.getFile().toPath());
            if (contentType == null) {
                contentType = determineContentType(material);
            }
        } catch (Exception e) {
            log.warn("Не удалось определить MIME-тип файла: {}", e.getMessage());
            contentType = determineContentType(material);
        }
        
        String encodedFileName = URLEncoder.encode(material.getTitle(), StandardCharsets.UTF_8)
            .replaceAll("\\+", "%20");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        
        // Для документов Office используем attachment вместо inline
        if (contentType.contains("vnd.openxmlformats") || contentType.contains("msword") || 
            contentType.contains("ms-excel") || contentType.contains("ms-powerpoint")) {
            headers.add("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        } else {
            headers.add("Content-Disposition", "inline; filename*=UTF-8''" + encodedFileName);
        }
        
        log.info("Отправка файла с заголовками: {}", headers);
        return ResponseEntity.ok()
            .headers(headers)
            .body(file);
    }

    private String determineContentType(Material material) {
        log.info("Определение MIME-типа для материала: {}", material);
        String fileName = material.getTitle().toLowerCase();
        
        if (fileName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (fileName.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (fileName.endsWith(".doc")) {
            return "application/msword";
        } else if (fileName.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if (fileName.endsWith(".xls")) {
            return "application/vnd.ms-excel";
        } else if (fileName.endsWith(".pptx")) {
            return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        } else if (fileName.endsWith(".ppt")) {
            return "application/vnd.ms-powerpoint";
        } else if (fileName.endsWith(".txt")) {
            return "text/plain";
        } else if (fileName.endsWith(".rtf")) {
            return "application/rtf";
        } else if (fileName.endsWith(".mp4")) {
            return "video/mp4";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else {
            log.warn("Неизвестный тип файла: {}", fileName);
            return "application/octet-stream";
        }
    }

    @GetMapping("/course/{courseId}/with-files")
    public ResponseEntity<List<Material>> getMaterialsByCourseWithFiles(@PathVariable Long courseId) {
        List<Material> materials = materialService.getMaterialsByCourse(courseId);
        for (Material material : materials) {
            try {
                List<String> urls = objectMapper.readValue(material.getUrl(), List.class);
                if (urls != null && !urls.isEmpty()) {
                    // Добавляем информацию о доступных файлах
                    material.setUrl(objectMapper.writeValueAsString(urls));
                }
            } catch (Exception e) {
                log.error("Ошибка при обработке URL файлов для материала {}: {}", material.getId(), e.getMessage());
            }
        }
        return ResponseEntity.ok(materials);
    }
} 