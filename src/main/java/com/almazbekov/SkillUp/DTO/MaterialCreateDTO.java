package com.almazbekov.SkillUp.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class MaterialCreateDTO {
    private Long courseId;
    private Long typeId;
    private String title;
    private String description;
    private MultipartFile file;
    private Integer orderIndex;
    private Integer duration;
} 