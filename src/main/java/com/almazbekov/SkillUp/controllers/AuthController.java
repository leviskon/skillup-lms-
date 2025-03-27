package com.almazbekov.SkillUp.controllers;

import com.almazbekov.SkillUp.DTO.LoginRequest;
import com.almazbekov.SkillUp.DTO.RegisterRequest;
import com.almazbekov.SkillUp.entity.Role;
import com.almazbekov.SkillUp.entity.User;
import com.almazbekov.SkillUp.repository.RoleRepository;
import com.almazbekov.SkillUp.repository.UserRepository;
import com.almazbekov.SkillUp.services.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    public ResponseEntity<?> login(@RequestBody LoginRequest request, 
                                 HttpSession session,
                                 HttpServletResponse httpResponse) {
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
            
            // Устанавливаем время жизни сессии
            session.setMaxInactiveInterval(3600); // 1 час

            // Создаем куки с правильными параметрами
            Cookie sessionCookie = new Cookie("JSESSIONID", session.getId());
            sessionCookie.setPath("/");
            sessionCookie.setHttpOnly(true);
            sessionCookie.setMaxAge(3600); // 1 час
            sessionCookie.setSecure(false); // установите true если используете HTTPS
            sessionCookie.setDomain("localhost"); // Добавляем домен
            httpResponse.addCookie(sessionCookie);

            // Логируем информацию о сессии и куках
            System.out.println("=== Информация о сессии после логина ===");
            System.out.println("ID сессии: " + session.getId());
            System.out.println("Время создания сессии: " + session.getCreationTime());
            System.out.println("Последний доступ к сессии: " + session.getLastAccessedTime());
            System.out.println("Максимальное время неактивности: " + session.getMaxInactiveInterval());
            System.out.println("Пользователь в сессии: " + session.getAttribute("user"));
            
            System.out.println("\n=== Куки после логина ===");
            System.out.println("JSESSIONID = " + session.getId());
            System.out.println("Path = /");
            System.out.println("HttpOnly = true");
            System.out.println("MaxAge = 3600");
            System.out.println("Domain = localhost");

            // Создаем ответ с данными пользователя
            Map<String, Object> response = new HashMap<>();
            response.put("user", user);

            return ResponseEntity.ok()
                .header("Set-Cookie", "JSESSIONID=" + session.getId() + "; Path=/; HttpOnly; Max-Age=3600; Domain=localhost")
                .body(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body("Неверный email или пароль");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request, 
                                        HttpSession session,
                                        HttpServletRequest httpRequest,
                                        HttpServletResponse httpResponse) {
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

        // Логируем информацию о сессии и куках
        System.out.println("=== Информация о сессии после регистрации ===");
        System.out.println("ID сессии: " + session.getId());
        System.out.println("Время создания сессии: " + session.getCreationTime());
        System.out.println("Последний доступ к сессии: " + session.getLastAccessedTime());
        System.out.println("Максимальное время неактивности: " + session.getMaxInactiveInterval());
        System.out.println("Пользователь в сессии: " + session.getAttribute("user"));
        
        // Логируем все куки
        System.out.println("\n=== Куки после регистрации ===");
        Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                System.out.println("Имя куки: " + cookie.getName());
                System.out.println("Значение куки: " + cookie.getValue());
                System.out.println("Домен: " + cookie.getDomain());
                System.out.println("Путь: " + cookie.getPath());
                System.out.println("Максимальный возраст: " + cookie.getMaxAge());
                System.out.println("HttpOnly: " + cookie.isHttpOnly());
                System.out.println("Secure: " + cookie.getSecure());
                System.out.println("---");
            }
        } else {
            System.out.println("Куки не найдены");
        }

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
