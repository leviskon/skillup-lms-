package com.almazbekov.SkillUp.controllers;

import com.almazbekov.SkillUp.DTO.CourseEnrollmentDTO;
import com.almazbekov.SkillUp.services.CourseEnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class CourseEnrollmentController {
    private final CourseEnrollmentService enrollmentService;

    @PostMapping("/courses/{courseId}/enroll")
    public ResponseEntity<CourseEnrollmentDTO> enrollInCourse(
            @PathVariable Long courseId,
            @RequestParam Long studentId) {
        return ResponseEntity.ok(enrollmentService.enrollStudentInCourse(courseId, studentId));
    }

    @DeleteMapping("/students/{studentId}/unenroll")
    public ResponseEntity<Void> unenrollFromCourse(
            @PathVariable Long studentId,
            @RequestParam Long courseId) {
        enrollmentService.unenrollStudentFromCourse(courseId, studentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/students/{studentId}")
    public ResponseEntity<List<CourseEnrollmentDTO>> getStudentEnrollments(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(enrollmentService.getStudentEnrollments(studentId));
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<List<CourseEnrollmentDTO>> getCourseEnrollments(
            @PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.getCourseEnrollments(courseId));
    }

    @GetMapping
    public ResponseEntity<List<CourseEnrollmentDTO>> getAllEnrollments() {
        return ResponseEntity.ok(enrollmentService.getAllEnrollments());
    }
} 