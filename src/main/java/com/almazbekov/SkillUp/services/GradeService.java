package com.almazbekov.SkillUp.services;

import com.almazbekov.SkillUp.DTO.GradeCreateDTO;
import com.almazbekov.SkillUp.DTO.GradeResponseDTO;
import com.almazbekov.SkillUp.entity.Grade;
import com.almazbekov.SkillUp.entity.Submission;
import com.almazbekov.SkillUp.entity.User;
import com.almazbekov.SkillUp.repository.GradeRepository;
import com.almazbekov.SkillUp.repository.SubmissionRepository;
import com.almazbekov.SkillUp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeService {
    private final GradeRepository gradeRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    @Transactional
    public GradeResponseDTO createGrade(GradeCreateDTO gradeDTO) {
        Submission submission = submissionRepository.findById(gradeDTO.getSubmissionId())
                .orElseThrow(() -> new RuntimeException("Отправка не найдена"));

        User gradedBy = userRepository.findById(gradeDTO.getGradedById())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Проверяем, не существует ли уже оценка для этой отправки
        if (gradeRepository.findBySubmissionId(gradeDTO.getSubmissionId()).isPresent()) {
            throw new RuntimeException("Оценка для этой отправки уже существует");
        }

        Grade grade = new Grade();
        grade.setSubmission(submission);
        grade.setGrade(gradeDTO.getGrade());
        grade.setFeedback(gradeDTO.getFeedback());
        grade.setGradedBy(gradedBy);

        return GradeResponseDTO.fromEntity(gradeRepository.save(grade));
    }

    @Transactional
    public GradeResponseDTO updateGrade(Long gradeId, GradeCreateDTO gradeDTO) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new RuntimeException("Оценка не найдена"));

        User gradedBy = userRepository.findById(gradeDTO.getGradedById())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        grade.setGrade(gradeDTO.getGrade());
        grade.setFeedback(gradeDTO.getFeedback());
        grade.setGradedBy(gradedBy);

        return GradeResponseDTO.fromEntity(gradeRepository.save(grade));
    }

    @Transactional(readOnly = true)
    public GradeResponseDTO getGradeById(Long gradeId) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new RuntimeException("Оценка не найдена"));
        return GradeResponseDTO.fromEntity(grade);
    }

    @Transactional(readOnly = true)
    public GradeResponseDTO getGradeBySubmissionId(Long submissionId) {
        Grade grade = gradeRepository.findBySubmissionId(submissionId)
                .orElseThrow(() -> new RuntimeException("Оценка не найдена"));
        return GradeResponseDTO.fromEntity(grade);
    }

    @Transactional(readOnly = true)
    public List<GradeResponseDTO> getGradesByGradedBy(Long gradedById) {
        return gradeRepository.findByGradedById(gradedById).stream()
                .map(GradeResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteGrade(Long gradeId) {
        gradeRepository.deleteById(gradeId);
    }
} 