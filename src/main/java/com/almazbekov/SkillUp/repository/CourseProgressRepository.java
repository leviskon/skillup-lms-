package com.almazbekov.SkillUp.repository;

import com.almazbekov.SkillUp.entity.CourseProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseProgressRepository extends JpaRepository<CourseProgress, Long> {
} 