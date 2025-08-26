-- Миграция для изменения типа owner_user_id на UUID в order-service

-- Создаем временную функцию для конвертации Long в UUID
CREATE OR REPLACE FUNCTION long_to_uuid(long_id BIGINT) RETURNS UUID AS $$
BEGIN
    RETURN md5(long_id::text || 'user-salt-2024')::uuid;
END;
$$ LANGUAGE plpgsql;

-- Изменяем тип колонки owner_user_id в таблице orders с конвертацией
ALTER TABLE orders.orders ALTER COLUMN owner_user_id TYPE UUID USING long_to_uuid(owner_user_id::BIGINT);

-- Добавляем ограничения для UUID
ALTER TABLE orders.orders ALTER COLUMN owner_user_id SET NOT NULL;

-- Удаляем временную функцию
DROP FUNCTION IF EXISTS long_to_uuid(BIGINT);
