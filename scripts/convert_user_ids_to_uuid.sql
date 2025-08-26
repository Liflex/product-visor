-- Скрипт для конвертации существующих Long ID в UUID
-- ВНИМАНИЕ: Выполнять только после создания резервной копии!

-- Функция для конвертации Long в UUID
CREATE OR REPLACE FUNCTION long_to_uuid(long_id BIGINT) RETURNS UUID AS $$
BEGIN
    -- Используем детерминированный способ конвертации Long в UUID
    -- Это обеспечит одинаковые UUID для одинаковых Long ID
    RETURN md5(long_id::text || 'user-salt-2024')::uuid;
END;
$$ LANGUAGE plpgsql;

-- Конвертируем существующие данные в client.user
UPDATE client.user 
SET id = long_to_uuid(id::BIGINT) 
WHERE id IS NOT NULL;

-- Конвертируем существующие данные в client.user_company
UPDATE client.user_company 
SET user_id = long_to_uuid(user_id::BIGINT) 
WHERE user_id IS NOT NULL;

-- Конвертируем существующие данные в visor.product
UPDATE visor.product 
SET owner_user_id = long_to_uuid(owner_user_id::BIGINT) 
WHERE owner_user_id IS NOT NULL;

-- Конвертируем существующие данные в orders.orders
UPDATE orders.orders 
SET owner_user_id = long_to_uuid(owner_user_id::BIGINT) 
WHERE owner_user_id IS NOT NULL;

-- Удаляем временную функцию
DROP FUNCTION IF EXISTS long_to_uuid(BIGINT);

