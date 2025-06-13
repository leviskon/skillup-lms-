package com.almazbekov.SkillUp.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GradeCreateDTO {
    @NotNull(message = "ID отправки не может быть пустым")
    private Long submissionId;

    @NotNull(message = "Оценка не может быть пустой")
    @Min(value = 0, message = "Оценка не может быть меньше 0")
    @Max(value = 100, message = "Оценка не может быть больше 100")
    private Integer grade;

    private String feedback;

    @NotNull(message = "ID оценивающего не может быть пустым")
    private Long gradedById;
} 