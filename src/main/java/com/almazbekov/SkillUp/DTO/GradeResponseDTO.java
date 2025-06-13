package com.almazbekov.SkillUp.DTO;

import com.almazbekov.SkillUp.entity.Grade;
import com.almazbekov.SkillUp.entity.Submission;
import com.almazbekov.SkillUp.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GradeResponseDTO {
    private Long id;
    private Submission submission;
    private Integer grade;
    private String feedback;
    private User gradedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static GradeResponseDTO fromEntity(Grade grade) {
        GradeResponseDTO dto = new GradeResponseDTO();
        dto.setId(grade.getId());
        dto.setSubmission(grade.getSubmission());
        dto.setGrade(grade.getGrade());
        dto.setFeedback(grade.getFeedback());
        dto.setGradedBy(grade.getGradedBy());
        dto.setCreatedAt(grade.getCreatedAt());
        dto.setUpdatedAt(grade.getUpdatedAt());
        return dto;
    }
} 