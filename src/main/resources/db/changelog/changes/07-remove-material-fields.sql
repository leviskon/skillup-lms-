-- Удаляем столбцы order_index и duration из таблицы materials
ALTER TABLE materials DROP COLUMN order_index;
ALTER TABLE materials DROP COLUMN duration; 