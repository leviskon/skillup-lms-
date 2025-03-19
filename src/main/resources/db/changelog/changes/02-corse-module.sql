ALTER TABLE users
    ADD COLUMN avatar_url TEXT;

ALTER TABLE courses
    ADD COLUMN level VARCHAR(50),
    ADD COLUMN category VARCHAR(100),
    ADD COLUMN tags TEXT[],
    ADD COLUMN total_students INTEGER DEFAULT 0;

ALTER TABLE materials
    ADD COLUMN duration INTEGER,
    ADD COLUMN is_published BOOLEAN DEFAULT false,
    ADD COLUMN published_at TIMESTAMP;

ALTER TABLE assignments
    ADD COLUMN max_score INTEGER DEFAULT 100,
    ADD COLUMN is_published BOOLEAN DEFAULT false,
    ADD COLUMN published_at TIMESTAMP;

ALTER TABLE grades
    ADD COLUMN graded_by INTEGER NOT NULL,
    ADD CONSTRAINT fk_grade_graded_by FOREIGN KEY (graded_by) REFERENCES users(id) ON DELETE CASCADE;
