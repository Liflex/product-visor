-- Добавление warehouse_id в таблицу orders
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name='orders'
        AND column_name='warehouse_id'
    ) THEN
        ALTER TABLE orders ADD COLUMN warehouse_id varchar;
        
        -- Добавляем комментарий к колонке
        COMMENT ON COLUMN orders.warehouse_id IS 'ID склада для заказа';
        
        -- Создаем индекс для улучшения производительности запросов по warehouse_id
        CREATE INDEX IF NOT EXISTS idx_orders_warehouse_id ON orders(warehouse_id);
    END IF;
END $$;
