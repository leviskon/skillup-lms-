package com.almazbekov.SkillUp.controllers;

import com.almazbekov.SkillUp.DTO.CourseCreateDTO;
import com.almazbekov.SkillUp.entity.Course;
import com.almazbekov.SkillUp.entity.User;
import com.almazbekov.SkillUp.services.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;

import java.io.IOException;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<Course> createCourse(
            @ModelAttribute CourseCreateDTO courseDTO,
            HttpSession session,
            HttpServletRequest request) throws IOException {

        // Логируем информацию о запросе
        System.out.println("=== Информация о запросе создания курса ===");
        System.out.println("Сессия ID: " + session.getId());
        System.out.println("Пользователь в сессии: " + session.getAttribute("user"));
        
        // Логируем куки
        System.out.println("\n=== Куки в запросе ===");
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                System.out.println("Имя куки: " + cookie.getName());
                System.out.println("Значение куки: " + cookie.getValue());
                System.out.println("---");
            }
        } else {
            System.out.println("Куки не найдены в запросе");
        }

        // Получаем пользователя из сессии
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new IllegalStateException("Ошибка: пользователь не аутентифицирован. Необходимо войти в систему.");
        }

        System.out.println("\n=== Информация о пользователе ===");
        System.out.println("ID пользователя: " + user.getId());
        System.out.println("Роль пользователя: " + user.getRole().getName());

        return ResponseEntity.ok(courseService.createCourse(courseDTO, user));
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable Long courseId,
            @ModelAttribute CourseCreateDTO courseDTO) throws IOException {
        return ResponseEntity.ok(courseService.updateCourse(courseId, courseDTO));
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) throws IOException {
        courseService.deleteCourse(courseId);
        return ResponseEntity.ok().build();
    }
} 