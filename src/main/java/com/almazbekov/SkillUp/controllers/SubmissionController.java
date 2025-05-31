package com.almazbekov.SkillUp.controllers;

import com.almazbekov.SkillUp.DTO.SubmissionCreateDTO;
import com.almazbekov.SkillUp.DTO.SubmissionResponseDTO;
import com.almazbekov.SkillUp.entity.Submission;
import com.almazbekov.SkillUp.security.CustomUserDetails;
import com.almazbekov.SkillUp.services.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private static final Logger log = LoggerFactory.getLogger(SubmissionController.class);

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Submission> createSubmission(
            @ModelAttribute SubmissionCreateDTO submissionDTO) throws IOException {
        log.info("Создание новой отправки для задания: {}", submissionDTO.getAssignmentId());
        Submission submission = submissionService.createSubmission(submissionDTO);
        return ResponseEntity.ok(submission);
    }

    @PutMapping("/{submissionId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Submission> updateSubmission(
            @PathVariable Long submissionId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("studentId") Long studentId) throws IOException {
        log.info("Обновление отправки: {}", submissionId);
        Submission submission = submissionService.updateSubmission(submissionId, file, studentId);
        return ResponseEntity.ok(submission);
    }

    @DeleteMapping("/{submissionId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> deleteSubmission(@PathVariable Long submissionId) throws IOException {
        log.info("Удаление отправки: {}", submissionId);
        submissionService.deleteSubmission(submissionId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<List<SubmissionResponseDTO>> getSubmissionsByAssignmentId(
            @PathVariable Long assignmentId) {
        List<SubmissionResponseDTO> submissions = submissionService.getSubmissionsByAssignmentId(assignmentId);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<Submission>> getSubmissionsByStudent(
            @RequestParam("studentId") Long studentId) {
        log.info("Получение отправок студента");
        List<Submission> submissions = submissionService.getSubmissionsByStudent(studentId);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/{submissionId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    public ResponseEntity<Submission> getSubmissionById(@PathVariable Long submissionId) {
        log.info("Получение отправки: {}", submissionId);
        Submission submission = submissionService.getSubmissionById(submissionId);
        return ResponseEntity.ok(submission);
    }

    @GetMapping("/{submissionId}/file")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    public ResponseEntity<Resource> getSubmissionFile(@PathVariable Long submissionId) throws IOException {
        log.info("Получение файла отправки: {}", submissionId);
        
        Submission submission = submissionService.getSubmissionById(submissionId);
        if (submission == null) {
            log.error("Отправка с ID {} не найдена", submissionId);
            return ResponseEntity.notFound().build();
        }
        
        Resource file = submissionService.getSubmissionFile(submissionId);
        if (file == null || !file.exists()) {
            log.error("Файл не найден для отправки с ID {}", submissionId);
            return ResponseEntity.notFound().build();
        }
        
        String contentType;
        try {
            contentType = Files.probeContentType(file.getFile().toPath());
            if (contentType == null) {
                contentType = determineContentType(submission);
            }
        } catch (Exception e) {
            log.warn("Не удалось определить MIME-тип файла: {}", e.getMessage());
            contentType = determineContentType(submission);
        }
        
        String encodedFileName = URLEncoder.encode(submission.getFileUrl(), StandardCharsets.UTF_8)
            .replaceAll("\\+", "%20");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        
        if (contentType.contains("vnd.openxmlformats") || contentType.contains("msword") || 
            contentType.contains("ms-excel") || contentType.contains("ms-powerpoint")) {
            headers.add("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        } else {
            headers.add("Content-Disposition", "inline; filename*=UTF-8''" + encodedFileName);
        }
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(file);
    }

    private String determineContentType(Submission submission) {
        log.info("Определение MIME-типа для отправки: {}", submission);
        String fileName = submission.getFileUrl().toLowerCase();
        
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
} 