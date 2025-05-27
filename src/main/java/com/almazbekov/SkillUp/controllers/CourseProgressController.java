package com.almazbekov.SkillUp.controllers;

import com.almazbekov.SkillUp.DTO.CourseProgressDTO;
import com.almazbekov.SkillUp.entity.CourseProgress;
import com.almazbekov.SkillUp.services.CourseProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/course-progress")
@RequiredArgsConstructor
public class CourseProgressController {

    private final CourseProgressService courseProgressService;

    @PostMapping
    public ResponseEntity<CourseProgressDTO> createOrUpdateCourseProgress(
            @RequestBody CourseProgressDTO requestDTO) {
        CourseProgressDTO progress = courseProgressService.createOrUpdateCourseProgress(
                requestDTO);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<CourseProgressDTO>> getProgressByStudent(@PathVariable Long studentId) {
        List<CourseProgressDTO> progressList = courseProgressService.getProgressByStudent(studentId);
        return ResponseEntity.ok(progressList);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CourseProgressDTO>> getProgressByCourse(@PathVariable Long courseId) {
        List<CourseProgressDTO> progressList = courseProgressService.getProgressByCourse(courseId);
        return ResponseEntity.ok(progressList);
    }

    @GetMapping("/student/{studentId}/course/{courseId}")
    public ResponseEntity<?> getProgressByStudentAndCourse(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        Optional<CourseProgress> progressOptional = courseProgressService.getProgressByStudentAndCourse(studentId, courseId);
        
        if (progressOptional.isPresent()) {
            CourseProgress progress = progressOptional.get();
             CourseProgressDTO dto = new CourseProgressDTO();
            dto.setId(progress.getId());
            dto.setStudentId(progress.getStudent().getId());
            dto.setCourseId(progress.getCourse().getId());
            dto.setCompletedMaterials(progress.getCompletedMaterials());
            dto.setTotalMaterials(progress.getTotalMaterials());
            dto.setLastAccessedAt(progress.getLastAccessedAt());
            dto.setCreatedAt(progress.getCreatedAt());
            dto.setUpdatedAt(progress.getUpdatedAt());

            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/student/{studentId}/course/{courseId}")
    public ResponseEntity<Void> deleteProgressByStudentAndCourse(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        courseProgressService.deleteProgressByStudentAndCourse(studentId, courseId);
        return ResponseEntity.ok().build();
    }
} 