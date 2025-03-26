package com.almazbekov.SkillUp.repository;

import com.almazbekov.SkillUp.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByUserId(Long userId);
} 