ALTER TABLE notifications
    ADD COLUMN course_id INTEGER,
    ADD CONSTRAINT fk_notification_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE;