package com.almazbekov.SkillUp.DTO;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String name;
    private String role; // "STUDENT" или "TEACHER"
}
