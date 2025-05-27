package com.almazbekov.SkillUp.services;

import com.almazbekov.SkillUp.DTO.CourseProgressDTO;
import com.almazbekov.SkillUp.entity.CourseProgress;
import com.almazbekov.SkillUp.repository.CourseProgressRepository;
import com.almazbekov.SkillUp.repository.CourseRepository;
import com.almazbekov.SkillUp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseProgressService {

    private final CourseProgressRepository courseProgressRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public CourseProgressDTO createOrUpdateCourseProgress(CourseProgressDTO requestDTO) {
        // Find existing progress or create a new one
        CourseProgress progress = courseProgressRepository.findByStudentIdAndCourseId(requestDTO.getStudentId(), requestDTO.getCourseId())
                .orElseGet(() -> {
                    // Ensure student and course exist before creating progress
                    userRepository.findById(requestDTO.getStudentId())
                            .orElseThrow(() -> new RuntimeException("Student not found"));
                    courseRepository.findById(requestDTO.getCourseId())
                            .orElseThrow(() -> new RuntimeException("Course not found"));

                    CourseProgress newProgress = new CourseProgress();
                    newProgress.setStudent(userRepository.findById(requestDTO.getStudentId()).get());
                    newProgress.setCourse(courseRepository.findById(requestDTO.getCourseId()).get());
                    newProgress.setCompletedMaterials(null); // Начинаем с пустого списка ID
                    newProgress.setTotalMaterials(0); // Устанавливаем начальное значение
                    newProgress.setLastAccessedAt(LocalDateTime.now());
                    return newProgress;
                });

        // Update completed materials list if provided in DTO
        if (requestDTO.getCompletedMaterials() != null) {
            progress.setCompletedMaterials(requestDTO.getCompletedMaterials()); // Устанавливаем список ID напрямую
        }

         if (requestDTO.getTotalMaterials() != null) { // Обновляем totalMaterials, если предоставлен в DTO
            progress.setTotalMaterials(requestDTO.getTotalMaterials());
        }
        progress.setLastAccessedAt(LocalDateTime.now());

        // TODO: Implement logic to update totalMaterials based on course structure
        // This might require fetching course details and material count - ЭТОТ TODO ВСЕ ЕЩЕ АКТУАЛЕН ДЛЯ АВТОМАТИЧЕСКОГО ОПРЕДЕЛЕНИЯ totalMaterials

        CourseProgress savedProgress = courseProgressRepository.save(progress);
        return toDTO(savedProgress);
    }

    @Transactional(readOnly = true)
    public List<CourseProgressDTO> getProgressByStudent(Long studentId) {
        List<CourseProgress> progressList = courseProgressRepository.findByStudentId(studentId);
        return progressList.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseProgressDTO> getProgressByCourse(Long courseId) {
        List<CourseProgress> progressList = courseProgressRepository.findByCourseId(courseId);
        return progressList.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<CourseProgress> getProgressByStudentAndCourse(Long studentId, Long courseId) {
        return courseProgressRepository.findByStudentIdAndCourseId(studentId, courseId);
    }
    
    @Transactional
    public void deleteProgressByStudent(Long studentId) {
        courseProgressRepository.deleteByStudentId(studentId);
    }
    
    @Transactional // Добавляем транзакцию для операции удаления
    public void deleteProgressByStudentAndCourse(Long studentId, Long courseId) {
        courseProgressRepository.deleteByStudentIdAndCourseId(studentId, courseId);
    }

    // Helper method to convert Entity to DTO
    private CourseProgressDTO toDTO(CourseProgress progress) {
        CourseProgressDTO dto = new CourseProgressDTO();
        dto.setId(progress.getId());
        dto.setStudentId(progress.getStudent().getId());
        dto.setCourseId(progress.getCourse().getId());
        dto.setCompletedMaterials(progress.getCompletedMaterials());
        dto.setTotalMaterials(progress.getTotalMaterials());
        dto.setLastAccessedAt(progress.getLastAccessedAt());
        dto.setCreatedAt(progress.getCreatedAt());
        dto.setUpdatedAt(progress.getUpdatedAt());
        return dto;
    }

     // NOTE: The repository methods findByStudentId and findByCourseId are assumed to exist. You may need to add them to CourseProgressRepository.
} 