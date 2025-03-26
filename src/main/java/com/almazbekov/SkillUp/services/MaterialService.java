package com.almazbekov.SkillUp.services;

import com.almazbekov.SkillUp.DTO.MaterialCreateDTO;
import com.almazbekov.SkillUp.entity.Course;
import com.almazbekov.SkillUp.entity.Material;
import com.almazbekov.SkillUp.entity.MaterialType;
import com.almazbekov.SkillUp.repository.CourseRepository;
import com.almazbekov.SkillUp.repository.MaterialRepository;
import com.almazbekov.SkillUp.repository.MaterialTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final CourseRepository courseRepository;
    private final MaterialTypeRepository materialTypeRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public Material createMaterial(MaterialCreateDTO materialDTO) throws IOException {
        Course course = courseRepository.findById(materialDTO.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        MaterialType materialType = materialTypeRepository.findById(materialDTO.getTypeId())
                .orElseThrow(() -> new RuntimeException("Material type not found"));

        Material material = new Material();
        material.setCourse(course);
        material.setType(materialType);
        material.setTitle(materialDTO.getTitle());
        material.setDescription(materialDTO.getDescription());
        material.setOrderIndex(materialDTO.getOrderIndex());
        material.setDuration(materialDTO.getDuration());
        material.setPublished(false);

        // Определяем поддиректорию в зависимости от типа материала
        String subDirectory;
        if (materialType.getName().equals("VIDEO")) {
            subDirectory = "materials/videos";
        } else if (materialType.getName().equals("DOCUMENT")) {
            subDirectory = "materials/documents";
        } else {
            throw new IllegalArgumentException("Unsupported material type");
        }

        // Сохраняем файл
        String fileUrl = fileStorageService.storeFile(materialDTO.getFile(), subDirectory);
        material.setUrl(fileUrl);

        return materialRepository.save(material);
    }

    @Transactional
    public Material updateMaterial(Long materialId, MaterialCreateDTO materialDTO) throws IOException {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material not found"));

        material.setTitle(materialDTO.getTitle());
        material.setDescription(materialDTO.getDescription());
        material.setOrderIndex(materialDTO.getOrderIndex());
        material.setDuration(materialDTO.getDuration());

        // Обновляем файл, если он был изменен
        if (materialDTO.getFile() != null) {
            // Удаляем старый файл
            fileStorageService.deleteFile(material.getUrl());

            // Определяем поддиректорию в зависимости от типа материала
            String subDirectory;
            if (material.getType().getName().equals("VIDEO")) {
                subDirectory = "materials/videos";
            } else if (material.getType().getName().equals("DOCUMENT")) {
                subDirectory = "materials/documents";
            } else {
                throw new IllegalArgumentException("Unsupported material type");
            }

            // Сохраняем новый файл
            String fileUrl = fileStorageService.storeFile(materialDTO.getFile(), subDirectory);
            material.setUrl(fileUrl);
        }

        return materialRepository.save(material);
    }

    @Transactional
    public void deleteMaterial(Long materialId) throws IOException {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material not found"));

        // Удаляем файл
        fileStorageService.deleteFile(material.getUrl());

        materialRepository.delete(material);
    }
} 