-- Удаляем колонку warehouse_id из таблицы company_credentials
ALTER TABLE ozon.company_credentials DROP COLUMN IF EXISTS warehouse_id;
