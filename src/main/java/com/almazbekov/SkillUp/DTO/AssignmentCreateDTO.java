package com.almazbekov.SkillUp.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AssignmentCreateDTO {
    private Long courseId;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private List<MultipartFile> files;
} 