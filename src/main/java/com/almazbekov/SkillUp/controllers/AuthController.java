package com.almazbekov.SkillUp.controllers;

import com.almazbekov.SkillUp.DTO.RegisterRequest;
import com.almazbekov.SkillUp.entity.Role;
import com.almazbekov.SkillUp.entity.User;
import com.almazbekov.SkillUp.repository.RoleRepository;
import com.almazbekov.SkillUp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository; // Добавляем RoleRepository
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email уже зарегистрирован");
        }

        // Ищем роль в БД
        Optional<Role> roleOptional = roleRepository.findByName(request.getRole());
        if (roleOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Указанная роль не найдена");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Шифруем пароль
        user.setName(request.getName());
        user.setRole(roleOptional.get()); // Устанавливаем найденную роль

        userRepository.save(user);
        return ResponseEntity.ok("Пользователь зарегистрирован!");
    }
}
