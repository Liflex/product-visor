-- Удаляем ненужные колонки из таблицы company_credentials для Ozon
-- Ozon использует только client_id и api_key
ALTER TABLE ozon.company_credentials 
DROP COLUMN IF EXISTS client_secret,
DROP COLUMN IF EXISTS business_id;
