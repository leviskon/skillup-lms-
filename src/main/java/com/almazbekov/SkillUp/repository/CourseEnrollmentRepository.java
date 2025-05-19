package com.almazbekov.SkillUp.repository;

import com.almazbekov.SkillUp.entity.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    List<CourseEnrollment> findByCourseId(Long courseId);
    List<CourseEnrollment> findByStudentId(Long studentId);
    Optional<CourseEnrollment> findByCourseIdAndStudentId(Long courseId, Long studentId);
    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);
} 