package com.almazbekov.SkillUp.services;

import com.almazbekov.SkillUp.DTO.CourseCreateDTO;
import com.almazbekov.SkillUp.DTO.CourseDTO;
import com.almazbekov.SkillUp.entity.Course;
import com.almazbekov.SkillUp.entity.User;
import com.almazbekov.SkillUp.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public CourseDTO createCourse(CourseCreateDTO courseDTO, User user) throws IOException {
        // Проверяем, является ли пользователь учителем
        if (!"TEACHER".equals(user.getRole().getName())) {
            throw new RuntimeException("User is not a teacher");
        }

        Course course = new Course();
        course.setName(courseDTO.getName());
        course.setDescription(courseDTO.getDescription());
        course.setLevel(courseDTO.getLevel());
        course.setCategory(courseDTO.getCategory());
        course.setUser(user);
        course.setTotalStudents(0);
        course.setPublished(false);

        // Сохраняем изображение курса
        if (courseDTO.getImageFile() != null) {
            String imageUrl = fileStorageService.storeFile(courseDTO.getImageFile(), "courses/images");
            course.setImageUrl(imageUrl);
        }

        return convertToDTO(courseRepository.save(course));
    }

    @Transactional
    public CourseDTO updateCourse(Long courseId, CourseCreateDTO courseDTO) throws IOException {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        course.setName(courseDTO.getName());
        course.setDescription(courseDTO.getDescription());
        course.setLevel(courseDTO.getLevel());
        course.setCategory(courseDTO.getCategory());

        // Обновляем изображение курса
        if (courseDTO.getImageFile() != null) {
            // Удаляем старое изображение
            if (course.getImageUrl() != null) {
                fileStorageService.deleteFile(course.getImageUrl());
            }
            // Сохраняем новое изображение
            String imageUrl = fileStorageService.storeFile(courseDTO.getImageFile(), "courses/images");
            course.setImageUrl(imageUrl);
        }

        return convertToDTO(courseRepository.save(course));
    }

    @Transactional
    public void deleteCourse(Long courseId) throws IOException {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Удаляем изображение курса
        if (course.getImageUrl() != null) {
            fileStorageService.deleteFile(course.getImageUrl());
        }

        courseRepository.delete(course);
    }

    public List<CourseDTO> getCoursesByUserId(Long userId) {
        return courseRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CourseDTO getCourseById(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return convertToDTO(course);
    }

    private CourseDTO convertToDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setDescription(course.getDescription());
        dto.setImageUrl(course.getImageUrl());
        dto.setPublished(course.isPublished());
        dto.setLevel(course.getLevel());
        dto.setCategory(course.getCategory());
        dto.setTotalStudents(course.getTotalStudents());
        dto.setUserId(course.getUser().getId());
        dto.setUserName(course.getUser().getName());
        dto.setCreatedAt(course.getCreatedAt());
        dto.setUpdatedAt(course.getUpdatedAt());
        return dto;
    }
} 