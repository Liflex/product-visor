-- Создание схемы marketplace
CREATE SCHEMA IF NOT EXISTS marketplace;

-- Создание таблицы company_credentials в схеме marketplace
CREATE TABLE IF NOT EXISTS marketplace.company_credentials (
    id BIGSERIAL PRIMARY KEY,
    company_id UUID NOT NULL,
    marketplace VARCHAR(50) NOT NULL, -- "OZON", "YANDEX", etc.
    client_id VARCHAR(255) NOT NULL,
    api_key VARCHAR(255) NOT NULL, -- Для Ozon это API Key, для Yandex это может быть Client Secret
    access_token TEXT,
    refresh_token TEXT,
    token_expires_at TIMESTAMPTZ,
    business_id VARCHAR(255), -- Для Yandex
    warehouse_id VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Создание индексов
CREATE INDEX IF NOT EXISTS idx_company_credentials_company_marketplace 
ON marketplace.company_credentials(company_id, marketplace);

CREATE INDEX IF NOT EXISTS idx_company_credentials_marketplace_active 
ON marketplace.company_credentials(marketplace, is_active);

CREATE INDEX IF NOT EXISTS idx_company_credentials_company_id 
ON marketplace.company_credentials(company_id);

-- Создание уникального ограничения
CREATE UNIQUE INDEX IF NOT EXISTS uk_company_credentials_company_marketplace 
ON marketplace.company_credentials(company_id, marketplace) 
WHERE is_active = true;
