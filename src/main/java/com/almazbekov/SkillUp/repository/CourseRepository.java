package com.almazbekov.SkillUp.repository;

import com.almazbekov.SkillUp.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
} 