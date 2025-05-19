package com.almazbekov.SkillUp.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CourseEnrollmentDTO {
    private Long id;
    private Long courseId;
    private String courseName;
    private Long studentId;
    private String studentName;
    private LocalDateTime enrolledAt;
} 