-- Удаляем старый внешний ключ из courses
ALTER TABLE courses DROP CONSTRAINT fk_course_teacher;

-- Изменяем тип данных teacher_id, если нужно (например, если раньше он ссылался на users.id)
ALTER TABLE courses ALTER COLUMN teacher_id SET DATA TYPE INTEGER USING teacher_id::INTEGER;

-- Добавляем новый внешний ключ, который теперь ссылается на teachers.id
ALTER TABLE courses ADD CONSTRAINT fk_course_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE;
