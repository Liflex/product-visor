-- Создание таблицы истории статусов заказов
CREATE TABLE IF NOT EXISTS orders.order_status_history (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders.orders(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL,
    previous_status VARCHAR(50),
    changed_at TIMESTAMPTZ NOT NULL,
    reason TEXT,
    source VARCHAR(50)
);

-- Индексы для быстрого поиска
CREATE INDEX IF NOT EXISTS idx_order_status_history_order_id ON orders.order_status_history(order_id);
CREATE INDEX IF NOT EXISTS idx_order_status_history_changed_at ON orders.order_status_history(changed_at);
CREATE INDEX IF NOT EXISTS idx_order_status_history_status ON orders.order_status_history(status);

-- Комментарии к таблице
COMMENT ON TABLE orders.order_status_history IS 'История изменений статусов заказов';
COMMENT ON COLUMN orders.order_status_history.order_id IS 'ID заказа';
COMMENT ON COLUMN orders.order_status_history.status IS 'Новый статус';
COMMENT ON COLUMN orders.order_status_history.previous_status IS 'Предыдущий статус';
COMMENT ON COLUMN orders.order_status_history.changed_at IS 'Дата и время изменения';
COMMENT ON COLUMN orders.order_status_history.reason IS 'Причина изменения статуса';
COMMENT ON COLUMN orders.order_status_history.source IS 'Источник изменения (OZON_API, MANUAL, etc.)';
