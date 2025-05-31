package com.almazbekov.SkillUp.DTO;

import com.almazbekov.SkillUp.entity.Assignment;
import com.almazbekov.SkillUp.entity.Submission;
import com.almazbekov.SkillUp.entity.User;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SubmissionResponseDTO {
    private Submission submission;
    private Assignment assignment;
    private User student;
    private String fileUrl;
    private LocalDateTime submittedAt;
} 