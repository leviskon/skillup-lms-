package com.almazbekov.SkillUp.repository;

import com.almazbekov.SkillUp.entity.CourseProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseProgressRepository extends JpaRepository<CourseProgress, Long> {
    Optional<CourseProgress> findByStudentIdAndCourseId(Long studentId, Long courseId);
    List<CourseProgress> findByStudentId(Long studentId);
    List<CourseProgress> findByCourseId(Long courseId);
    
    void deleteByStudentId(Long studentId);
    void deleteByStudentIdAndCourseId(Long studentId, Long courseId);
} 