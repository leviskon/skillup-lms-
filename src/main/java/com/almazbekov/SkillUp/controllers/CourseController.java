package com.almazbekov.SkillUp.controllers;

import com.almazbekov.SkillUp.DTO.CourseCreateDTO;
import com.almazbekov.SkillUp.DTO.CourseDTO;
import com.almazbekov.SkillUp.entity.User;
import com.almazbekov.SkillUp.services.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(
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
    public ResponseEntity<CourseDTO> updateCourse(
            @PathVariable Long courseId,
            @ModelAttribute CourseCreateDTO courseDTO) throws IOException {
        return ResponseEntity.ok(courseService.updateCourse(courseId, courseDTO));
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Long courseId,
            HttpSession session) throws IOException {
        
        // Получаем пользователя из сессии
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new IllegalStateException("Ошибка: пользователь не аутентифицирован. Необходимо войти в систему.");
        }

        // Проверяем, является ли пользователь преподавателем
        if (!"TEACHER".equals(user.getRole().getName())) {
            throw new IllegalStateException("Ошибка: доступ запрещен. Только преподаватели могут удалять курсы.");
        }

        courseService.deleteCourse(courseId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-courses")
    public ResponseEntity<List<CourseDTO>> getMyCourses(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new IllegalStateException("Ошибка: пользователь не аутентифицирован. Необходимо войти в систему.");
        }

        // Проверяем, является ли пользователь преподавателем
        if (!"TEACHER".equals(user.getRole().getName())) {
            throw new IllegalStateException("Ошибка: доступ запрещен. Только преподаватели могут просматривать свои курсы.");
        }

        return ResponseEntity.ok(courseService.getCoursesByUserId(user.getId()));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseDTO> getCourseById(
            @PathVariable Long courseId,
            HttpSession session) {
        // Получаем пользователя из сессии
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new IllegalStateException("Ошибка: пользователь не аутентифицирован. Необходимо войти в систему.");
        }

        return ResponseEntity.ok(courseService.getCourseById(courseId));
    }
} 