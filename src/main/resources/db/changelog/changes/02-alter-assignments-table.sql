--liquibase formatted sql

--changeset author:alter-assignments-table:1
ALTER TABLE assignments
    ADD COLUMN title VARCHAR(255) NOT NULL DEFAULT 'Untitled Assignment',
    ADD COLUMN url VARCHAR(512),
    DROP COLUMN max_score;

--rollback ALTER TABLE assignments
--rollback     DROP COLUMN title,
--rollback     DROP COLUMN url,
--rollback     ADD COLUMN max_score INTEGER DEFAULT 100; 