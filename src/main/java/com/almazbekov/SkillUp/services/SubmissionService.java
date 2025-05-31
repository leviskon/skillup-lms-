package com.almazbekov.SkillUp.services;

import com.almazbekov.SkillUp.DTO.SubmissionCreateDTO;
import com.almazbekov.SkillUp.DTO.SubmissionResponseDTO;
import com.almazbekov.SkillUp.entity.Assignment;
import com.almazbekov.SkillUp.entity.Submission;
import com.almazbekov.SkillUp.entity.User;
import com.almazbekov.SkillUp.repository.AssignmentRepository;
import com.almazbekov.SkillUp.repository.SubmissionRepository;
import com.almazbekov.SkillUp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public Submission createSubmission(SubmissionCreateDTO submissionDTO) throws IOException {
        Assignment assignment = assignmentRepository.findById(submissionDTO.getAssignmentId())
                .orElseThrow(() -> new RuntimeException("Задание не найдено"));
            
        User student = userRepository.findById(submissionDTO.getStudentId())
                .orElseThrow(() -> new RuntimeException("Студент не найден"));

        String fileUrl = fileStorageService.storeFile(submissionDTO.getFile(), "submissions/files");
        
        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setFileUrl(fileUrl);
        
        return submissionRepository.save(submission);
    }

    @Transactional
    public Submission updateSubmission(Long submissionId, MultipartFile file, Long studentId) throws IOException {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Отправка не найдена"));

        if (!submission.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("Нет доступа к этой отправке");
        }

        // Удаляем старый файл
        fileStorageService.deleteFile(submission.getFileUrl());

        // Сохраняем новый файл
        String fileUrl = fileStorageService.storeFile(file, "submissions/files");
        submission.setFileUrl(fileUrl);

        return submissionRepository.save(submission);
    }

    @Transactional
    public void deleteSubmission(Long submissionId) throws IOException {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Отправка не найдена"));

        // Удаляем файл
        fileStorageService.deleteFile(submission.getFileUrl());

        submissionRepository.delete(submission);
    }

    @Transactional(readOnly = true)
    public List<Submission> getSubmissionsByAssignment(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId);
    }

    @Transactional(readOnly = true)
    public List<Submission> getSubmissionsByStudent(Long studentId) {
        return submissionRepository.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public Submission getSubmissionById(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Отправка не найдена"));
    }

    @Transactional(readOnly = true)
    public Resource getSubmissionFile(Long submissionId) throws IOException {
        Submission submission = getSubmissionById(submissionId);
        if (submission == null) {
            throw new RuntimeException("Отправка не найдена");
        }

        String fileUrl = submission.getFileUrl();
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            throw new RuntimeException("URL файла пустой");
        }

        // Формируем полный путь к файлу
        String fullPath = "submissions/files/" + fileUrl;
        
        Resource resource = fileStorageService.loadFileAsResource(fullPath);
        if (resource == null || !resource.exists()) {
            throw new RuntimeException("Файл не найден по указанному пути");
        }

        return resource;
    }

    public List<SubmissionResponseDTO> getSubmissionsByAssignmentId(Long assignmentId) {
        List<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId);
        return submissions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private SubmissionResponseDTO convertToDTO(Submission submission) {
        SubmissionResponseDTO dto = new SubmissionResponseDTO();
        dto.setSubmission(submission);
        dto.setAssignment(submission.getAssignment());
        dto.setStudent(submission.getStudent());
        return dto;
    }
} 