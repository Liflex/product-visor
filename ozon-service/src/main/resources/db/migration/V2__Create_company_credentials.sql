CREATE TABLE IF NOT EXISTS ozon.company_credentials (
    id BIGSERIAL PRIMARY KEY,
    company_id UUID NOT NULL,
    client_id TEXT NOT NULL,
    api_key TEXT NOT NULL,
    warehouse_id TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_company_credentials_company ON ozon.company_credentials(company_id);




