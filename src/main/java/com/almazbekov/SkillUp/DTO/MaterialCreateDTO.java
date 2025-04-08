package com.almazbekov.SkillUp.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class MaterialCreateDTO {
    private Long courseId;
    private Long typeId;
    private String title;
    private String description;
    private List<MultipartFile> files;
} 