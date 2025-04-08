package com.almazbekov.SkillUp.controllers;

import com.almazbekov.SkillUp.DTO.UserUpdateDTO;
import com.almazbekov.SkillUp.entity.User;
import com.almazbekov.SkillUp.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<User> getProfile(HttpSession session) {
        log.info("Получение профиля пользователя из сессии");
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            log.error("Пользователь не найден в сессии");
            return ResponseEntity.badRequest().build();
        }
        
        log.info("Профиль пользователя успешно получен из сессии: {}", user.getEmail());
        return ResponseEntity.ok(userService.getUserProfile(user.getId()));
    }

    @PutMapping
    public ResponseEntity<User> updateProfile(
            @ModelAttribute UserUpdateDTO updateDTO,
            HttpSession session) throws IOException {
        log.info("Попытка обновления профиля пользователя");
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            log.error("Пользователь не найден в сессии");
            return ResponseEntity.badRequest().build();
        }

        User updatedUser = userService.updateUserProfile(user.getId(), updateDTO);
        log.info("Профиль пользователя успешно обновлен: {}", updatedUser.getEmail());
        
        // Обновляем пользователя в сессии
        session.setAttribute("user", updatedUser);
        
        return ResponseEntity.ok(updatedUser);
    }
} 