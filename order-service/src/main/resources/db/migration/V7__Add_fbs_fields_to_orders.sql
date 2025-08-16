-- Добавление полей для FBS заказов
ALTER TABLE orders.orders ADD COLUMN IF NOT EXISTS in_process_at TIMESTAMPTZ;
ALTER TABLE orders.orders ADD COLUMN IF NOT EXISTS shipment_date TIMESTAMPTZ;
ALTER TABLE orders.orders ADD COLUMN IF NOT EXISTS delivering_date TIMESTAMPTZ;

-- Поля для отмены
ALTER TABLE orders.orders ADD COLUMN IF NOT EXISTS cancel_reason VARCHAR(500);
ALTER TABLE orders.orders ADD COLUMN IF NOT EXISTS cancel_reason_id BIGINT;
ALTER TABLE orders.orders ADD COLUMN IF NOT EXISTS cancellation_type VARCHAR(100);

-- Поля для доставки
ALTER TABLE orders.orders ADD COLUMN IF NOT EXISTS tracking_number VARCHAR(100);
ALTER TABLE orders.orders ADD COLUMN IF NOT EXISTS delivery_method_name VARCHAR(200);
ALTER TABLE orders.orders ADD COLUMN IF NOT EXISTS substatus VARCHAR(100);
ALTER TABLE orders.orders ADD COLUMN IF NOT EXISTS is_express BOOLEAN DEFAULT FALSE;

-- Индексы для поиска
CREATE INDEX IF NOT EXISTS idx_orders_shipment_date ON orders.orders(shipment_date);
CREATE INDEX IF NOT EXISTS idx_orders_delivering_date ON orders.orders(delivering_date);
CREATE INDEX IF NOT EXISTS idx_orders_in_process_at ON orders.orders(in_process_at);
CREATE INDEX IF NOT EXISTS idx_orders_substatus ON orders.orders(substatus);
CREATE INDEX IF NOT EXISTS idx_orders_is_express ON orders.orders(is_express);

-- Комментарии
COMMENT ON COLUMN orders.orders.in_process_at IS 'Дата начала обработки заказа (FBS)';
COMMENT ON COLUMN orders.orders.shipment_date IS 'Дата отправки заказа (FBS)';
COMMENT ON COLUMN orders.orders.delivering_date IS 'Дата начала доставки (FBS)';
COMMENT ON COLUMN orders.orders.cancel_reason IS 'Причина отмены заказа';
COMMENT ON COLUMN orders.orders.cancel_reason_id IS 'ID причины отмены';
COMMENT ON COLUMN orders.orders.cancellation_type IS 'Тип отмены заказа';
COMMENT ON COLUMN orders.orders.tracking_number IS 'Номер отслеживания доставки';
COMMENT ON COLUMN orders.orders.delivery_method_name IS 'Название способа доставки';
COMMENT ON COLUMN orders.orders.substatus IS 'Подстатус заказа';
COMMENT ON COLUMN orders.orders.is_express IS 'Экспресс доставка';
