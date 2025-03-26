package com.almazbekov.SkillUp.controllers;

import com.almazbekov.SkillUp.DTO.CourseCreateDTO;
import com.almazbekov.SkillUp.entity.Course;
import com.almazbekov.SkillUp.entity.User;
import com.almazbekov.SkillUp.services.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<Course> createCourse(
            @ModelAttribute CourseCreateDTO courseDTO,
            @AuthenticationPrincipal User teacher) throws IOException {
        return ResponseEntity.ok(courseService.createCourse(courseDTO, teacher));
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