package com.almazbekov.SkillUp.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SubmissionCreateDTO {
    private Long assignmentId;
    private Long studentId;
    private MultipartFile file;
} 