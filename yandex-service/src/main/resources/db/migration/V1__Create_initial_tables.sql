-- Создание таблицы для хранения учетных данных Yandex
CREATE TABLE IF NOT EXISTS yandex_credentials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    marketplace_name VARCHAR(50) NOT NULL DEFAULT 'Yandex',
    company_id UUID NOT NULL,
    user_id UUID NOT NULL,
    client_id VARCHAR(255) NOT NULL,
    api_key VARCHAR(255) NOT NULL,
    warehouse_id VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_sync_at TIMESTAMP,
    sync_status VARCHAR(50) DEFAULT 'NEVER_SYNCED',
    error_message TEXT
);

-- Создание индексов
CREATE INDEX IF NOT EXISTS idx_yandex_credentials_company_id ON yandex_credentials(company_id);
CREATE INDEX IF NOT EXISTS idx_yandex_credentials_user_id ON yandex_credentials(user_id);
CREATE INDEX IF NOT EXISTS idx_yandex_credentials_active ON yandex_credentials(is_active);

-- Создание таблицы для хранения точек синхронизации
CREATE TABLE IF NOT EXISTS yandex_sync_checkpoints (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    checkpoint_name VARCHAR(100) NOT NULL,
    company_id UUID NOT NULL,
    user_id UUID NOT NULL,
    last_sync_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    orders_processed INTEGER DEFAULT 0,
    sync_duration_ms BIGINT DEFAULT 0,
    error_message TEXT
);

-- Создание индексов для точек синхронизации
CREATE INDEX IF NOT EXISTS idx_yandex_sync_checkpoints_name ON yandex_sync_checkpoints(checkpoint_name);
CREATE INDEX IF NOT EXISTS idx_yandex_sync_checkpoints_company_id ON yandex_sync_checkpoints(company_id);
CREATE INDEX IF NOT EXISTS idx_yandex_sync_checkpoints_user_id ON yandex_sync_checkpoints(user_id);
