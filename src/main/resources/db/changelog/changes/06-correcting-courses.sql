-- Удаляем внешний ключ courses.teacher_id -> teachers.id
ALTER TABLE courses DROP CONSTRAINT fk_course_teacher;

-- Удаляем таблицу teachers
DROP TABLE teachers;

-- Добавляем новый внешний ключ courses.teacher_id -> users.id
ALTER TABLE courses ADD CONSTRAINT fk_course_teacher FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE;
