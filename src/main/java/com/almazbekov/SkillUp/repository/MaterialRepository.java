package com.almazbekov.SkillUp.repository;

import com.almazbekov.SkillUp.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
} 