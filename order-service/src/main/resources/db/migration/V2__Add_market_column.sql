-- Добавляем колонку market в таблицу orders (если она еще не существует)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'orders' 
        AND table_name = 'orders' 
        AND column_name = 'market'
    ) THEN
        ALTER TABLE orders.orders ADD COLUMN market VARCHAR(50);
    END IF;
END $$;

-- Создаем индекс для быстрого поиска по маркету (если он еще не существует)
CREATE INDEX IF NOT EXISTS idx_orders_market ON orders.orders(market);
