package com.almazbekov.SkillUp.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CourseCreateDTO {
    private String name;
    private String description;
    private String level;
    private String category;
    private MultipartFile imageFile;
} 