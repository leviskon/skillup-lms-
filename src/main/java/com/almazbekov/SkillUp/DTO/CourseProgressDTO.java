package com.almazbekov.SkillUp.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseProgressDTO {
    private Long id;
    private Long studentId;
    private Long courseId;
    private List<Long> completedMaterials;
    private Integer totalMaterials;
    private LocalDateTime lastAccessedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 