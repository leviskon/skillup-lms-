package com.almazbekov.SkillUp.controllers;

import com.almazbekov.SkillUp.DTO.GradeCreateDTO;
import com.almazbekov.SkillUp.DTO.GradeResponseDTO;
import com.almazbekov.SkillUp.services.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {
    private final GradeService gradeService;

    @PostMapping
    public ResponseEntity<GradeResponseDTO> createGrade(
            @Valid @RequestBody GradeCreateDTO gradeDTO) {
        return ResponseEntity.ok(gradeService.createGrade(gradeDTO));
    }

    @PutMapping("/{gradeId}")
    public ResponseEntity<GradeResponseDTO> updateGrade(
            @PathVariable Long gradeId,
            @Valid @RequestBody GradeCreateDTO gradeDTO) {
        return ResponseEntity.ok(gradeService.updateGrade(gradeId, gradeDTO));
    }

    @GetMapping("/{gradeId}")
    public ResponseEntity<GradeResponseDTO> getGradeById(@PathVariable Long gradeId) {
        return ResponseEntity.ok(gradeService.getGradeById(gradeId));
    }

    @GetMapping("/submission/{submissionId}")
    public ResponseEntity<GradeResponseDTO> getGradeBySubmissionId(@PathVariable Long submissionId) {
        return ResponseEntity.ok(gradeService.getGradeBySubmissionId(submissionId));
    }

    @GetMapping("/graded-by/{gradedById}")
    public ResponseEntity<List<GradeResponseDTO>> getGradesByGradedBy(@PathVariable Long gradedById) {
        return ResponseEntity.ok(gradeService.getGradesByGradedBy(gradedById));
    }

    @DeleteMapping("/{gradeId}")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long gradeId) {
        gradeService.deleteGrade(gradeId);
        return ResponseEntity.ok().build();
    }
} 