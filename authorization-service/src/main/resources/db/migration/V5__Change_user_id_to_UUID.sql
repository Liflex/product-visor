-- Миграция для изменения типа user_id на UUID в authorization-service

-- Создаем временную функцию для конвертации Long в UUID
CREATE OR REPLACE FUNCTION long_to_uuid(long_id BIGINT) RETURNS UUID AS $$
BEGIN
    RETURN md5(long_id::text || 'user-salt-2024')::uuid;
END;
$$ LANGUAGE plpgsql;

-- Удаляем существующий DEFAULT перед изменением типа
ALTER TABLE users ALTER COLUMN id DROP DEFAULT;

-- Изменяем тип колонки id в таблице users с конвертацией
ALTER TABLE users ALTER COLUMN id TYPE UUID USING long_to_uuid(id::BIGINT);

-- Добавляем новый DEFAULT для UUID
ALTER TABLE users ALTER COLUMN id SET DEFAULT gen_random_uuid();

-- Удаляем временную функцию
DROP FUNCTION IF EXISTS long_to_uuid(BIGINT);
