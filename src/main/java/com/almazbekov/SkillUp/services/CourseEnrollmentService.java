package com.almazbekov.SkillUp.services;

import com.almazbekov.SkillUp.DTO.CourseEnrollmentDTO;
import com.almazbekov.SkillUp.entity.Course;
import com.almazbekov.SkillUp.entity.CourseEnrollment;
import com.almazbekov.SkillUp.entity.User;
import com.almazbekov.SkillUp.repository.CourseEnrollmentRepository;
import com.almazbekov.SkillUp.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseEnrollmentService {
    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public CourseEnrollmentDTO enrollStudentInCourse(Long courseId, Long studentId) {
        // Проверяем, не записан ли уже студент на курс
        if (enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new RuntimeException("Student is already enrolled in this course");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setCourse(course);
        User student = new User();
        student.setId(studentId);
        enrollment.setStudent(student);

        // Увеличиваем количество студентов на курсе
        course.setTotalStudents(course.getTotalStudents() + 1);
        courseRepository.save(course);

        return convertToDTO(enrollmentRepository.save(enrollment));
    }

    @Transactional(readOnly = true)
    public List<CourseEnrollmentDTO> getStudentEnrollments(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseEnrollmentDTO> getCourseEnrollments(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void unenrollStudentFromCourse(Long courseId, Long studentId) {
        CourseEnrollment enrollment = enrollmentRepository.findByCourseIdAndStudentId(courseId, studentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        Course course = enrollment.getCourse();
        course.setTotalStudents(course.getTotalStudents() - 1);
        courseRepository.save(course);

        enrollmentRepository.delete(enrollment);
    }

    private CourseEnrollmentDTO convertToDTO(CourseEnrollment enrollment) {
        CourseEnrollmentDTO dto = new CourseEnrollmentDTO();
        dto.setId(enrollment.getId());
        dto.setCourseId(enrollment.getCourse().getId());
        dto.setCourseName(enrollment.getCourse().getName());
        dto.setStudentId(enrollment.getStudent().getId());
        dto.setStudentName(enrollment.getStudent().getName());
        dto.setEnrolledAt(enrollment.getEnrolledAt());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<CourseEnrollmentDTO> getAllEnrollments() {
        return enrollmentRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
} 