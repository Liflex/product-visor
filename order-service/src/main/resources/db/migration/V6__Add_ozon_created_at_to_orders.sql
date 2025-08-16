-- Добавление поля ozon_created_at в таблицу заказов
ALTER TABLE orders.orders 
ADD COLUMN IF NOT EXISTS ozon_created_at TIMESTAMPTZ;

-- Индекс для быстрого поиска по дате создания в Ozon
CREATE INDEX IF NOT EXISTS idx_orders_ozon_created_at ON orders.orders(ozon_created_at);

-- Комментарий к полю
COMMENT ON COLUMN orders.orders.ozon_created_at IS 'Дата создания заказа в Ozon (внешняя система)';
