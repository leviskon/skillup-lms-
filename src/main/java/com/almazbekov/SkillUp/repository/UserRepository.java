package com.almazbekov.SkillUp.repository;

import com.almazbekov.SkillUp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email); // Проверка, существует ли email
    Optional<User> findByEmail(String email); // Найти пользователя по email
}