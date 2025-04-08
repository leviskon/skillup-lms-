package com.almazbekov.SkillUp.controllers;

import com.almazbekov.SkillUp.DTO.UserUpdateDTO;
import com.almazbekov.SkillUp.entity.User;
import com.almazbekov.SkillUp.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<User> getProfile(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(userService.getUserProfile(user.getId()));
    }

    @PutMapping
    public ResponseEntity<User> updateProfile(
            @ModelAttribute UserUpdateDTO updateDTO,
            HttpSession session) throws IOException {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        User updatedUser = userService.updateUserProfile(user.getId(), updateDTO);
        
        // Обновляем пользователя в сессии
        session.setAttribute("user", updatedUser);
        
        return ResponseEntity.ok(updatedUser);
    }
} 