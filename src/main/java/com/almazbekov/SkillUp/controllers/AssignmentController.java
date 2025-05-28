package com.almazbekov.SkillUp.controllers;

import com.almazbekov.SkillUp.DTO.AssignmentCreateDTO;
import com.almazbekov.SkillUp.entity.Assignment;
import com.almazbekov.SkillUp.services.AssignmentService;
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
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private static final Logger log = LoggerFactory.getLogger(AssignmentController.class);
    private final ObjectMapper objectMapper;

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Assignment>> getAssignmentsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByCourse(courseId));
    }

    @GetMapping("/{assignmentId}")
    public ResponseEntity<Assignment> getAssignmentById(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(assignmentService.getAssignmentById(assignmentId));
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Assignment> createAssignment(
            @ModelAttribute AssignmentCreateDTO assignmentDTO) throws IOException {
        log.info("Создание задания. Входящие данные: courseId={}, title={}, description={}, dueDate={}, files={}",
                assignmentDTO.getCourseId(),
                assignmentDTO.getTitle(),
                assignmentDTO.getDescription(),
                assignmentDTO.getDueDate(),
                assignmentDTO.getFiles() != null ? assignmentDTO.getFiles().size() : 0);
        log.info("Тип данных dueDate: {}", assignmentDTO.getDueDate() != null ? assignmentDTO.getDueDate().getClass().getName() : "null");
        log.info("Сырые данные формы: {}", assignmentDTO);
        return ResponseEntity.ok(assignmentService.createAssignment(assignmentDTO));
    }

    @PutMapping("/{assignmentId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Assignment> updateAssignment(
            @PathVariable Long assignmentId,
            @ModelAttribute AssignmentCreateDTO assignmentDTO) throws IOException {
        log.info("Обновление задания {}. Входящие данные: courseId={}, title={}, description={}, dueDate={}",
                assignmentId,
                assignmentDTO.getCourseId(),
                assignmentDTO.getTitle(),
                assignmentDTO.getDescription(),
                assignmentDTO.getDueDate());
        return ResponseEntity.ok(assignmentService.updateAssignment(assignmentId, assignmentDTO));
    }

    @DeleteMapping("/{assignmentId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long assignmentId) throws IOException {
        assignmentService.deleteAssignment(assignmentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{assignmentId}/file/{fileIndex}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> getAssignmentFile(
            @PathVariable Long assignmentId,
            @PathVariable int fileIndex) throws IOException {
        log.info("Получение файла {} для задания с ID: {}", fileIndex, assignmentId);
        
        Assignment assignment = assignmentService.getAssignmentById(assignmentId);
        if (assignment == null) {
            log.error("Задание с ID {} не найдено", assignmentId);
            return ResponseEntity.notFound().build();
        }
        
        List<String> urls;
        try {
            urls = objectMapper.readValue(assignment.getUrl(), List.class);
        } catch (Exception e) {
            log.error("Ошибка при чтении URL файлов: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
        
        if (urls == null || urls.isEmpty()) {
            log.error("Нет файлов для задания с ID {}", assignmentId);
            return ResponseEntity.notFound().build();
        }
        
        if (fileIndex < 0 || fileIndex >= urls.size()) {
            log.error("Неверный индекс файла: {} для задания с ID {}", fileIndex, assignmentId);
            return ResponseEntity.badRequest().build();
        }
        
        Resource file = assignmentService.getAssignmentFile(assignmentId, fileIndex);
        if (file == null || !file.exists()) {
            log.error("Файл не найден для задания с ID {} и индексом {}", assignmentId, fileIndex);
            return ResponseEntity.notFound().build();
        }
        
        String contentType;
        try {
            contentType = Files.probeContentType(file.getFile().toPath());
            if (contentType == null) {
                contentType = determineContentType(assignment);
            }
        } catch (Exception e) {
            log.warn("Не удалось определить MIME-тип файла: {}", e.getMessage());
            contentType = determineContentType(assignment);
        }
        
        String encodedFileName = URLEncoder.encode(assignment.getTitle(), StandardCharsets.UTF_8)
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

    private String determineContentType(Assignment assignment) {
        log.info("Определение MIME-типа для задания: {}", assignment);
        String fileName = assignment.getTitle().toLowerCase();
        
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Assignment>> getAssignmentsByCourseWithFiles(@PathVariable Long courseId) {
        List<Assignment> assignments = assignmentService.getAssignmentsByCourse(courseId);
        for (Assignment assignment : assignments) {
            try {
                List<String> urls = objectMapper.readValue(assignment.getUrl(), List.class);
                if (urls != null && !urls.isEmpty()) {
                    // Добавляем информацию о доступных файлах
                    assignment.setUrl(objectMapper.writeValueAsString(urls));
                }
            } catch (Exception e) {
                log.error("Ошибка при обработке URL файлов для задания {}: {}", assignment.getId(), e.getMessage());
            }
        }
        return ResponseEntity.ok(assignments);
    }
} 