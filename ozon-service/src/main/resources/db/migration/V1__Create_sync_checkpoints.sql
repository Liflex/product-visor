-- Создание схемы ozon если не существует
CREATE SCHEMA IF NOT EXISTS ozon;

-- Создание таблицы точек синхронизации
CREATE TABLE IF NOT EXISTS ozon.sync_checkpoints (
    id BIGSERIAL PRIMARY KEY,
    checkpoint_name VARCHAR(100) NOT NULL UNIQUE,
    last_sync_at TIMESTAMP NOT NULL,
    last_order_id BIGINT,
    last_posting_number VARCHAR(100),
    orders_processed INTEGER DEFAULT 0,
    sync_duration_ms BIGINT,
    status VARCHAR(20) DEFAULT 'SUCCESS',
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Комментарии к таблице
COMMENT ON TABLE ozon.sync_checkpoints IS 'Точки синхронизации с внешними API';
COMMENT ON COLUMN ozon.sync_checkpoints.checkpoint_name IS 'Название точки синхронизации (FBO_ORDERS, PRODUCTS, etc.)';
COMMENT ON COLUMN ozon.sync_checkpoints.last_sync_at IS 'Дата и время последней успешной синхронизации';
COMMENT ON COLUMN ozon.sync_checkpoints.last_order_id IS 'ID последнего обработанного заказа';
COMMENT ON COLUMN ozon.sync_checkpoints.last_posting_number IS 'Номер последнего постинга';
COMMENT ON COLUMN ozon.sync_checkpoints.orders_processed IS 'Количество обработанных заказов в последней синхронизации';
COMMENT ON COLUMN ozon.sync_checkpoints.sync_duration_ms IS 'Длительность синхронизации в миллисекундах';
COMMENT ON COLUMN ozon.sync_checkpoints.status IS 'Статус синхронизации (SUCCESS, FAILED, IN_PROGRESS)';
COMMENT ON COLUMN ozon.sync_checkpoints.error_message IS 'Сообщение об ошибке, если есть';
