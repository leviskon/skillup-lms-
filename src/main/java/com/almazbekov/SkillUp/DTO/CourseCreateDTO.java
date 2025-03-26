package com.almazbekov.SkillUp.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class CourseCreateDTO {
    private String name;
    private String description;
    private String level;
    private String category;
    private List<String> tags;
    private MultipartFile imageFile;
} 