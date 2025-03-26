package com.almazbekov.SkillUp.controllers;

import com.almazbekov.SkillUp.DTO.MaterialCreateDTO;
import com.almazbekov.SkillUp.entity.Material;
import com.almazbekov.SkillUp.services.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @PostMapping
    public ResponseEntity<Material> createMaterial(
            @ModelAttribute MaterialCreateDTO materialDTO) throws IOException {
        return ResponseEntity.ok(materialService.createMaterial(materialDTO));
    }

    @PutMapping("/{materialId}")
    public ResponseEntity<Material> updateMaterial(
            @PathVariable Long materialId,
            @ModelAttribute MaterialCreateDTO materialDTO) throws IOException {
        return ResponseEntity.ok(materialService.updateMaterial(materialId, materialDTO));
    }

    @DeleteMapping("/{materialId}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long materialId) throws IOException {
        materialService.deleteMaterial(materialId);
        return ResponseEntity.ok().build();
    }
} 