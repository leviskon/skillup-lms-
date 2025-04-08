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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;
    private static final Logger log = LoggerFactory.getLogger(MaterialController.class);

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

    @GetMapping("/{materialId}/file")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Resource> getMaterialFile(@PathVariable Long materialId) throws IOException {
        log.info("Получение файла для материала с ID: {}", materialId);
        
        Material material = materialService.getMaterialById(materialId);
        log.info("Найден материал: {}", material);
        
        Resource file = materialService.getMaterialFile(materialId);
        log.info("Файл получен: {}", file);
        
        String contentType = determineContentType(material);
        log.info("Определен MIME-тип: {}", contentType);
        
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
        String typeName = material.getType().getName().toUpperCase();
        String fileName = material.getUrl().toLowerCase();
        
        log.info("Анализ файла: {}", fileName);
        
        switch (typeName) {
            case "VIDEO":
                return "video/mp4";
            case "DOCUMENT":
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
                } else {
                    log.warn("Неизвестный тип документа: {}", fileName);
                    return "application/octet-stream";
                }
            case "IMAGE":
                if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                    return "image/jpeg";
                } else if (fileName.endsWith(".png")) {
                    return "image/png";
                } else if (fileName.endsWith(".gif")) {
                    return "image/gif";
                } else {
                    return "image/jpeg";
                }
            default:
                log.warn("Неизвестный тип файла: {}", typeName);
                return "application/octet-stream";
        }
    }
} 