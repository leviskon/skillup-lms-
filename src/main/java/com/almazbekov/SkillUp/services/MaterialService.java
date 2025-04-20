package com.almazbekov.SkillUp.services;

import com.almazbekov.SkillUp.DTO.MaterialCreateDTO;
import com.almazbekov.SkillUp.entity.Course;
import com.almazbekov.SkillUp.entity.Material;
import com.almazbekov.SkillUp.entity.MaterialType;
import com.almazbekov.SkillUp.repository.CourseRepository;
import com.almazbekov.SkillUp.repository.MaterialRepository;
import com.almazbekov.SkillUp.repository.MaterialTypeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final CourseRepository courseRepository;
    private final MaterialTypeRepository materialTypeRepository;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        material.setPublished(false);

        // Определяем поддиректорию в зависимости от типа материала
        String subDirectory = materialType.getName().equals("VIDEO") ? "materials/videos" : "materials/documents";

        // Сохраняем все файлы
        List<String> fileUrls = new ArrayList<>();
        if (materialDTO.getFiles() != null) {
            for (MultipartFile file : materialDTO.getFiles()) {
                String fileUrl = fileStorageService.storeFile(file, subDirectory);
                fileUrls.add(fileUrl);
            }
        }

        // Преобразуем список URL'ов в JSON
        String jsonUrls = objectMapper.writeValueAsString(fileUrls);
        material.setUrl(jsonUrls);

        return materialRepository.save(material);
    }

    @Transactional
    public Material updateMaterial(Long materialId, MaterialCreateDTO materialDTO) throws IOException {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material not found"));

        material.setTitle(materialDTO.getTitle());
        material.setDescription(materialDTO.getDescription());

        // Обновляем файлы, если они были изменены
        if (materialDTO.getFiles() != null && !materialDTO.getFiles().isEmpty()) {
            // Удаляем старые файлы
            List<String> oldUrls = objectMapper.readValue(material.getUrl(), List.class);
            for (String oldUrl : oldUrls) {
                fileStorageService.deleteFile(oldUrl);
            }

            // Определяем поддиректорию в зависимости от типа материала
            String subDirectory;
            if (material.getType().getName().equals("VIDEO")) {
                subDirectory = "materials/videos";
            } else if (material.getType().getName().equals("DOCUMENT")) {
                subDirectory = "materials/documents";
            } else {
                throw new IllegalArgumentException("Unsupported material type");
            }

            // Сохраняем новые файлы
            List<String> newUrls = new ArrayList<>();
            for (MultipartFile file : materialDTO.getFiles()) {
                String fileUrl = fileStorageService.storeFile(file, subDirectory);
                newUrls.add(fileUrl);
            }

            // Сохраняем новые URL'ы в JSON
            String jsonUrls = objectMapper.writeValueAsString(newUrls);
            material.setUrl(jsonUrls);
        }

        return materialRepository.save(material);
    }

    @Transactional
    public void deleteMaterial(Long materialId) throws IOException {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material not found"));

        // Удаляем все файлы
        List<String> urls = objectMapper.readValue(material.getUrl(), List.class);
        for (String url : urls) {
            fileStorageService.deleteFile(url);
        }

        materialRepository.delete(material);
    }

    @Transactional(readOnly = true)
    public List<Material> getMaterialsByCourse(Long courseId) {
        return materialRepository.findByCourseId(courseId);
    }

    @Transactional(readOnly = true)
    public Material getMaterialById(Long materialId) {
        return materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Материал не найден"));
    }

    @Transactional(readOnly = true)
    public List<Material> getMaterialsByCourseAndType(Long courseId, Long typeId) {
        return materialRepository.findByCourseIdAndTypeId(courseId, typeId);
    }

    @Transactional(readOnly = true)
    public Resource getMaterialFile(Long materialId, int fileIndex) throws IOException {
        Material material = getMaterialById(materialId);
        if (material == null) {
            throw new RuntimeException("Материал не найден");
        }

        List<String> urls;
        try {
            urls = objectMapper.readValue(material.getUrl(), List.class);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при чтении URL файлов: " + e.getMessage());
        }

        if (urls == null || urls.isEmpty()) {
            throw new RuntimeException("Нет файлов для материала");
        }

        if (fileIndex < 0 || fileIndex >= urls.size()) {
            throw new RuntimeException("Неверный индекс файла");
        }

        String fileUrl = urls.get(fileIndex);
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            throw new RuntimeException("URL файла пустой");
        }

        // Определяем поддиректорию на основе типа материала
        String subDirectory = material.getType().getName().equals("VIDEO") ? 
            "materials/videos/" : "materials/documents/";
            
        // Если URL уже содержит поддиректорию, используем его как есть
        if (!fileUrl.startsWith("materials/")) {
            // Если нет, добавляем поддиректорию
            fileUrl = subDirectory + fileUrl;
        }

        Resource resource = fileStorageService.loadFileAsResource(fileUrl);
        if (resource == null || !resource.exists()) {
            throw new RuntimeException("Файл не найден: " + fileUrl);
        }

        return resource;
    }
}