package com.almazbekov.SkillUp.repository;

import com.almazbekov.SkillUp.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
} 