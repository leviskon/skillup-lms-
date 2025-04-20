ALTER TABLE notifications
    ADD COLUMN to_user_id INTEGER,
    ADD CONSTRAINT fk_notification_to_user FOREIGN KEY (to_user_id) REFERENCES users(id) ON DELETE CASCADE;