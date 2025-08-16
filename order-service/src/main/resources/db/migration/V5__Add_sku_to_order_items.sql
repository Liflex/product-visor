-- Добавление поля SKU в таблицу товаров заказа
ALTER TABLE orders.order_items 
ADD COLUMN IF NOT EXISTS sku VARCHAR(100);

-- Индекс для быстрого поиска по SKU
CREATE INDEX IF NOT EXISTS idx_order_items_sku ON orders.order_items(sku);

-- Комментарий к полю
COMMENT ON COLUMN orders.order_items.sku IS 'SKU товара из внешней системы (Ozon, etc.)';
