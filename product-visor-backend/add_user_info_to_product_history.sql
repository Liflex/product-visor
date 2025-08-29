-- Добавление полей userId и companyId в таблицу product_history
-- Миграция для отслеживания пользователя и компании, которые совершили изменение

-- Добавляем колонки для пользователя и компании
ALTER TABLE visor.product_history 
ADD COLUMN IF NOT EXISTS user_id UUID,
ADD COLUMN IF NOT EXISTS company_id UUID;

-- Добавляем комментарии к колонкам
COMMENT ON COLUMN visor.product_history.user_id IS 'ID пользователя, который совершил изменение';
COMMENT ON COLUMN visor.product_history.company_id IS 'ID компании, если применимо';

-- Создаем индексы для быстрого поиска по пользователю и компании
CREATE INDEX IF NOT EXISTS idx_product_history_user_id ON visor.product_history(user_id);
CREATE INDEX IF NOT EXISTS idx_product_history_company_id ON visor.product_history(company_id);
CREATE INDEX IF NOT EXISTS idx_product_history_user_company ON visor.product_history(user_id, company_id);

-- Проверяем результат
SELECT 'Migration completed successfully' as status;
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_schema = 'visor' 
AND table_name = 'product_history' 
AND column_name IN ('user_id', 'company_id');
