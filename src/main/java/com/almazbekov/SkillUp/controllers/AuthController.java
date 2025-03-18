package com.almazbekov.SkillUp.controllers;

import com.almazbekov.SkillUp.DTO.LoginRequest;
import com.almazbekov.SkillUp.DTO.RegisterRequest;
import com.almazbekov.SkillUp.entity.Role;
import com.almazbekov.SkillUp.entity.User;
import com.almazbekov.SkillUp.repository.RoleRepository;
import com.almazbekov.SkillUp.repository.UserRepository;
import com.almazbekov.SkillUp.services.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.authentication.AuthenticationManager;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        try {
            // Аутентифицируем пользователя
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Получаем пользователя
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            // Сохраняем пользователя в сессии
            session.setAttribute("user", user);

            // Создаем ответ с данными пользователя
            Map<String, Object> response = new HashMap<>();
            response.put("user", user);

            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body("Неверный email или пароль");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request, HttpSession session) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email уже зарегистрирован");
        }

        Optional<Role> roleOptional = roleRepository.findByName(request.getRole());
        if (roleOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Указанная роль не найдена");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setRole(roleOptional.get());

        userRepository.save(user);

        // Сохраняем пользователя в сессии
        session.setAttribute("user", user);

        // Создаем ответ с данными пользователя
        Map<String, Object> response = new HashMap<>();
        response.put("user", user);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Вы вышли из системы!");
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body("Пользователь не авторизован");
        }
        return ResponseEntity.ok(user);
    }

}
