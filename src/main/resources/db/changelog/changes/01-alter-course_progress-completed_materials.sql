-- Удаляем старую колонку INTEGER
ALTER TABLE course_progress DROP COLUMN completed_materials;

-- Добавляем новую колонку с типом BIGINT[] и значением по умолчанию пустой массив
ALTER TABLE course_progress ADD COLUMN completed_materials BIGINT[] DEFAULT '{}'; 