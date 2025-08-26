-- Скрипт для исправления проблем с миграциями Flyway
-- Выполните этот скрипт в базе данных product_visor

-- Очищаем историю миграций для authorization-service
DELETE FROM authorization.flyway_schema_history WHERE version > '3';

-- Очищаем историю миграций для client-service  
DELETE FROM client.flyway_schema_history WHERE version > '8';

-- Проверяем результат
SELECT 'authorization' as schema, version, description, success 
FROM authorization.flyway_schema_history 
ORDER BY version;

SELECT 'client' as schema, version, description, success 
FROM client.flyway_schema_history 
ORDER BY version;

