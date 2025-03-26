package com.almazbekov.SkillUp.services;

import com.almazbekov.SkillUp.DTO.CourseCreateDTO;
import com.almazbekov.SkillUp.entity.Course;
import com.almazbekov.SkillUp.entity.User;
import com.almazbekov.SkillUp.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public Course createCourse(CourseCreateDTO courseDTO, User teacher) throws IOException {
        Course course = new Course();
        course.setName(courseDTO.getName());
        course.setDescription(courseDTO.getDescription());
        course.setLevel(courseDTO.getLevel());
        course.setCategory(courseDTO.getCategory());
        course.setTags(courseDTO.getTags());
        course.setTeacher(teacher);
        course.setTotalStudents(0);
        course.setPublished(false);

        // Сохраняем изображение курса
        if (courseDTO.getImageFile() != null) {
            String imageUrl = fileStorageService.storeFile(courseDTO.getImageFile(), "courses/images");
            course.setImageUrl(imageUrl);
        }

        return courseRepository.save(course);
    }

    @Transactional
    public Course updateCourse(Long courseId, CourseCreateDTO courseDTO) throws IOException {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        course.setName(courseDTO.getName());
        course.setDescription(courseDTO.getDescription());
        course.setLevel(courseDTO.getLevel());
        course.setCategory(courseDTO.getCategory());
        course.setTags(courseDTO.getTags());

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

        return courseRepository.save(course);
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
} 