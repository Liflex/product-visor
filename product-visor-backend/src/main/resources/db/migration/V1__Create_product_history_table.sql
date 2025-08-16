-- Создание таблицы для истории изменений продукта
CREATE TABLE IF NOT EXISTS visor.product_history (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    field_name VARCHAR(50) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    change_reason VARCHAR(100) NOT NULL,
    source_system VARCHAR(50),
    source_id VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    metadata TEXT
);

-- Индексы для быстрого поиска
CREATE INDEX IF NOT EXISTS idx_product_history_product_id ON visor.product_history(product_id);
CREATE INDEX IF NOT EXISTS idx_product_history_field_name ON visor.product_history(field_name);
CREATE INDEX IF NOT EXISTS idx_product_history_change_reason ON visor.product_history(change_reason);
CREATE INDEX IF NOT EXISTS idx_product_history_source_system ON visor.product_history(source_system);
CREATE INDEX IF NOT EXISTS idx_product_history_source_id ON visor.product_history(source_id);
CREATE INDEX IF NOT EXISTS idx_product_history_created_at ON visor.product_history(created_at);
CREATE INDEX IF NOT EXISTS idx_product_history_product_field ON visor.product_history(product_id, field_name);
CREATE INDEX IF NOT EXISTS idx_product_history_reason_date ON visor.product_history(change_reason, created_at);

-- Комментарии к таблице и колонкам
COMMENT ON TABLE visor.product_history IS 'История изменений продукта';
COMMENT ON COLUMN visor.product_history.id IS 'Уникальный идентификатор записи истории';
COMMENT ON COLUMN visor.product_history.product_id IS 'ID продукта';
COMMENT ON COLUMN visor.product_history.field_name IS 'Название измененного поля (quantity, name, price, etc.)';
COMMENT ON COLUMN visor.product_history.old_value IS 'Предыдущее значение поля';
COMMENT ON COLUMN visor.product_history.new_value IS 'Новое значение поля';
COMMENT ON COLUMN visor.product_history.change_reason IS 'Причина изменения (ORDER_CREATED, ORDER_CANCELLED, MANUAL_UPDATE, etc.)';
COMMENT ON COLUMN visor.product_history.source_system IS 'Система-источник изменения (KAFKA, REST_API, MANUAL, etc.)';
COMMENT ON COLUMN visor.product_history.source_id IS 'ID источника (posting_number, user_id, etc.)';
COMMENT ON COLUMN visor.product_history.created_at IS 'Дата и время изменения';
COMMENT ON COLUMN visor.product_history.metadata IS 'Дополнительные данные в формате JSON';
