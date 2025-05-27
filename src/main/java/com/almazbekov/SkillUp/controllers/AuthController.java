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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
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
            log.info("Попытка входа пользователя с email: {}", request.getEmail());
            
            // Аутентифицируем пользователя
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            // Создаем и устанавливаем контекст безопасности
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            // Сохраняем контекст безопасности в сессии
            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

            // Получаем пользователя
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            // Сохраняем пользователя в сессии
            session.setAttribute("user", user);
            
            // Устанавливаем время жизни сессии (24 часа)
            session.setMaxInactiveInterval(86400);

            // Создаем куки с правильными параметрами
            Cookie sessionCookie = new Cookie("JSESSIONID", session.getId());
            sessionCookie.setPath("/");
            sessionCookie.setHttpOnly(true);
            sessionCookie.setMaxAge(86400);
            sessionCookie.setSecure(false);
            sessionCookie.setDomain("localhost");
            httpResponse.addCookie(sessionCookie);

            // Логируем информацию о сессии
            log.info("Сессия успешно создана: ID={}, время создания={}, время жизни={}",
                    session.getId(), session.getCreationTime(), session.getMaxInactiveInterval());
            log.info("Пользователь успешно аутентифицирован: email={}, роль={}",
                    user.getEmail(), user.getRole().getName());
            log.info("Куки успешно установлены: JSESSIONID={}, Path=/, HttpOnly=true, MaxAge=86400, Domain=localhost",
                    session.getId());

            // Создаем ответ с данными пользователя
            Map<String, Object> response = new HashMap<>();
            response.put("user", user);

            return ResponseEntity.ok()
                .header("Set-Cookie", "JSESSIONID=" + session.getId() + "; Path=/; HttpOnly; Max-Age=86400; Domain=localhost")
                .body(response);
        } catch (AuthenticationException e) {
            log.error("Ошибка аутентификации пользователя: email={}, причина={}",
                    request.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().body("Неверный email или пароль");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request, 
                                        HttpSession session,
                                        HttpServletRequest httpRequest,
                                        HttpServletResponse httpResponse) {
        log.info("Попытка регистрации нового пользователя: email={}, имя={}, роль={}",
                request.getEmail(), request.getName(), request.getRole());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("Email уже зарегистрирован: {}", request.getEmail());
            return ResponseEntity.badRequest().body("Email уже зарегистрирован");
        }

        Optional<Role> roleOptional = roleRepository.findByName(request.getRole());
        if (roleOptional.isEmpty()) {
            log.error("Роль не найдена: {}", request.getRole());
            return ResponseEntity.badRequest().body("Указанная роль не найдена");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setRole(roleOptional.get());

        User savedUser = userRepository.save(user);
        log.info("Пользователь успешно зарегистрирован: email={}, имя={}, роль={}",
                savedUser.getEmail(), savedUser.getName(), savedUser.getRole().getName());

        // Сохраняем пользователя в сессии
        session.setAttribute("user", savedUser);

        // Логируем информацию о сессии
        log.info("Сессия успешно создана: ID={}, время создания={}, время жизни={}",
                session.getId(), session.getCreationTime(), session.getMaxInactiveInterval());
        log.info("Куки успешно установлены: JSESSIONID={}, Path=/, HttpOnly=true, MaxAge=86400, Domain=localhost",
                session.getId());

        return ResponseEntity.ok(savedUser);
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

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
}
