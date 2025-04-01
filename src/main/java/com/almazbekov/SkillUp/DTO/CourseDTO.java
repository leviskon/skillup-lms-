package com.almazbekov.SkillUp.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CourseDTO {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private boolean isPublished;
    private String level;
    private String category;
    private int totalStudents;
    private Long userId;
    private String userName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 