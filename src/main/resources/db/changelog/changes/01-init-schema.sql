-- Создание таблицы прогресса студента по курсу
CREATE TABLE course_progress (
                                 id SERIAL PRIMARY KEY,
                                 student_id INTEGER NOT NULL,
                                 course_id INTEGER NOT NULL,
                                 completed_materials INTEGER DEFAULT 0,
                                 total_materials INTEGER DEFAULT 0,
                                 last_accessed_at TIMESTAMP DEFAULT now(),
                                 created_at TIMESTAMP DEFAULT now(),
                                 updated_at TIMESTAMP DEFAULT now(),
                                 CONSTRAINT fk_progress_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
                                 CONSTRAINT fk_progress_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
                                 CONSTRAINT unique_progress UNIQUE (student_id, course_id)
);

-- Создание таблицы уведомлений
CREATE TABLE notifications (
                               id SERIAL PRIMARY KEY,
                               user_id INTEGER NOT NULL,
                               title VARCHAR(255) NOT NULL,
                               message TEXT NOT NULL,
                               type VARCHAR(50) NOT NULL,
                               is_read BOOLEAN DEFAULT false,
                               created_at TIMESTAMP DEFAULT now(),
                               CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Добавление новых полей в таблицу materials
ALTER TABLE materials
    ADD COLUMN title VARCHAR(255) NOT NULL,
    ADD COLUMN description TEXT,
    ADD COLUMN order_index INTEGER DEFAULT 0;

-- Добавление новых полей в таблицу courses
ALTER TABLE courses
    ADD COLUMN image_url TEXT,
    ADD COLUMN is_published BOOLEAN DEFAULT false;