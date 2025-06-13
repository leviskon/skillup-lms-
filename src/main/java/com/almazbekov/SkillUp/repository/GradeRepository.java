package com.almazbekov.SkillUp.repository;

import com.almazbekov.SkillUp.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    Optional<Grade> findBySubmissionId(Long submissionId);
    List<Grade> findByGradedById(Long gradedById);
} 