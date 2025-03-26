package com.almazbekov.SkillUp.services;

import com.almazbekov.SkillUp.entity.Teacher;
import com.almazbekov.SkillUp.entity.User;
import com.almazbekov.SkillUp.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;

    @Transactional
    public Teacher createTeacher(User user) {
        Teacher teacher = new Teacher();
        teacher.setUser(user);
        return teacherRepository.save(teacher);
    }

    public Teacher getTeacherByUserId(Long userId) {
        return teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
    }
} 