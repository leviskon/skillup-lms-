package com.almazbekov.SkillUp.services;

import com.almazbekov.SkillUp.DTO.AssignmentCreateDTO;
import com.almazbekov.SkillUp.entity.Assignment;
import com.almazbekov.SkillUp.entity.Course;
import com.almazbekov.SkillUp.repository.AssignmentRepository;
import com.almazbekov.SkillUp.repository.CourseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(AssignmentService.class);

    @Transactional
    public Assignment createAssignment(AssignmentCreateDTO assignmentDTO) throws IOException {
        log.info("Создание задания в сервисе. DueDate до установки: {}", assignmentDTO.getDueDate());
        
        Course course = courseRepository.findById(assignmentDTO.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Assignment assignment = new Assignment();
        assignment.setCourse(course);
        assignment.setTitle(assignmentDTO.getTitle());
        assignment.setDescription(assignmentDTO.getDescription());
        assignment.setDueDate(assignmentDTO.getDueDate());
        log.info("DueDate после установки в сущность: {}", assignment.getDueDate());
        assignment.setPublished(false);

        // Сохраняем все файлы
        List<String> fileUrls = new ArrayList<>();
        if (assignmentDTO.getFiles() != null) {
            for (MultipartFile file : assignmentDTO.getFiles()) {
                String fileUrl = fileStorageService.storeFile(file, "assignments/files");
                fileUrls.add(fileUrl);
            }
        }

        // Преобразуем список URL'ов в JSON
        String jsonUrls = objectMapper.writeValueAsString(fileUrls);
        assignment.setUrl(jsonUrls);

        Assignment savedAssignment = assignmentRepository.save(assignment);
        log.info("Задание сохранено. DueDate в сохраненной сущности: {}", savedAssignment.getDueDate());
        return savedAssignment;
    }

    @Transactional
    public Assignment updateAssignment(Long assignmentId, AssignmentCreateDTO assignmentDTO) throws IOException {
        log.info("Обновление задания в сервисе. DueDate до обновления: {}", assignmentDTO.getDueDate());
        
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        assignment.setTitle(assignmentDTO.getTitle());
        assignment.setDescription(assignmentDTO.getDescription());
        assignment.setDueDate(assignmentDTO.getDueDate());
        log.info("DueDate после установки в сущность: {}", assignment.getDueDate());

        // Обновляем файлы, если они были изменены
        if (assignmentDTO.getFiles() != null && !assignmentDTO.getFiles().isEmpty()) {
            // Удаляем старые файлы
            List<String> oldUrls = objectMapper.readValue(assignment.getUrl(), List.class);
            for (String oldUrl : oldUrls) {
                fileStorageService.deleteFile(oldUrl);
            }

            // Сохраняем новые файлы
            List<String> newUrls = new ArrayList<>();
            for (MultipartFile file : assignmentDTO.getFiles()) {
                String fileUrl = fileStorageService.storeFile(file, "assignments/files");
                newUrls.add(fileUrl);
            }

            // Сохраняем новые URL'ы в JSON
            String jsonUrls = objectMapper.writeValueAsString(newUrls);
            assignment.setUrl(jsonUrls);
        }

        Assignment savedAssignment = assignmentRepository.save(assignment);
        log.info("Задание обновлено. DueDate в сохраненной сущности: {}", savedAssignment.getDueDate());
        return savedAssignment;
    }

    @Transactional
    public void deleteAssignment(Long assignmentId) throws IOException {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        // Удаляем все файлы
        List<String> urls = objectMapper.readValue(assignment.getUrl(), List.class);
        for (String url : urls) {
            fileStorageService.deleteFile(url);
        }

        assignmentRepository.delete(assignment);
    }

    @Transactional(readOnly = true)
    public List<Assignment> getAssignmentsByCourse(Long courseId) {
        return assignmentRepository.findByCourseId(courseId);
    }

    @Transactional(readOnly = true)
    public Assignment getAssignmentById(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
    }

    @Transactional(readOnly = true)
    public Resource getAssignmentFile(Long assignmentId, int fileIndex) throws IOException {
        Assignment assignment = getAssignmentById(assignmentId);
        if (assignment == null) {
            throw new RuntimeException("Assignment not found");
        }

        List<String> urls;
        try {
            urls = objectMapper.readValue(assignment.getUrl(), List.class);
        } catch (Exception e) {
            throw new RuntimeException("Error reading file URLs: " + e.getMessage());
        }

        if (urls == null || urls.isEmpty()) {
            throw new RuntimeException("No files for assignment");
        }

        if (fileIndex < 0 || fileIndex >= urls.size()) {
            throw new RuntimeException("Invalid file index");
        }

        String fileUrl = urls.get(fileIndex);
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            throw new RuntimeException("Empty file URL");
        }

        // Формируем полный путь к файлу
        String fullPath = "assignments/files/" + fileUrl;
        
        Resource resource = fileStorageService.loadFileAsResource(fullPath);
        if (resource == null || !resource.exists()) {
            throw new RuntimeException("File not found at specified path");
        }

        return resource;
    }
} 