package com.almazbekov.SkillUp.services;

import com.almazbekov.SkillUp.DTO.UserUpdateDTO;
import com.almazbekov.SkillUp.entity.Role;
import com.almazbekov.SkillUp.entity.User;
import com.almazbekov.SkillUp.repository.RoleRepository;
import com.almazbekov.SkillUp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;  // Репозиторий для работы с ролями

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Метод для регистрации нового пользователя.
     * Если пользователь с данным email уже существует, выбрасывается исключение.
     */
    public User registerUser(String email, String password) {
        // Проверяем, существует ли пользователь с таким email
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already taken");
        }

        // Получаем роль USER из базы данных
        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Хешируем пароль
        String encodedPassword = passwordEncoder.encode(password);

        // Создаём нового пользователя
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(encodedPassword);
        newUser.setRole(role);

        // Сохраняем пользователя в базу данных
        return userRepository.save(newUser);
    }

    /**
     * Метод для загрузки пользователя по email, необходимый для аутентификации Spring Security.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Преобразуем User в объект UserDetails
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                AuthorityUtils.createAuthorityList(user.getRole().getName())
        );
    }

    /**
     * Метод для обновления профиля пользователя
     */
    public User updateUserProfile(Long userId, UserUpdateDTO updateDTO) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Проверяем, не занят ли email другим пользователем
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateDTO.getEmail())) {
                throw new RuntimeException("Email уже занят");
            }
            user.setEmail(updateDTO.getEmail());
        }

        // Обновляем имя, если оно предоставлено
        if (updateDTO.getName() != null) {
            user.setName(updateDTO.getName());
        }

        // Обрабатываем загрузку аватара
        if (updateDTO.getAvatarFile() != null && !updateDTO.getAvatarFile().isEmpty()) {
            String avatarUrl = fileStorageService.storeFile(updateDTO.getAvatarFile(), "avatars");
            user.setAvatarUrl("/avatars/" + avatarUrl);
        }

        return userRepository.save(user);
    }

    /**
     * Метод для получения информации о пользователе
     */
    public User getUserProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }
}
