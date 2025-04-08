package com.almazbekov.SkillUp.repository;

import com.almazbekov.SkillUp.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findByCourseId(Long courseId);
    List<Material> findByCourseIdAndTypeId(Long courseId, Long typeId);
} 