package com.almazbekov.SkillUp.repository;

import com.almazbekov.SkillUp.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name); // Найти роль по названию
}