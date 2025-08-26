-- Создание таблицы warehouses
CREATE TABLE warehouses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    user_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    warehouse_type VARCHAR(10) NOT NULL CHECK (warehouse_type IN ('FBS', 'FBO')),
    external_warehouse_id VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_home_warehouse BOOLEAN NOT NULL DEFAULT false,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Создание таблицы product_stocks
CREATE TABLE product_stocks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id BIGINT NOT NULL,
    warehouse_id UUID NOT NULL,
    user_id UUID NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    last_sync_at TIMESTAMP,
    sync_status VARCHAR(50) DEFAULT 'NEVER_SYNCED',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(product_id, warehouse_id)
);

-- Создание индексов
CREATE INDEX idx_warehouses_company_id ON warehouses(company_id);
CREATE INDEX idx_warehouses_user_id ON warehouses(user_id);
CREATE INDEX idx_warehouses_home_warehouse ON warehouses(is_home_warehouse);
CREATE INDEX idx_product_stocks_product_id ON product_stocks(product_id);
CREATE INDEX idx_product_stocks_warehouse_id ON product_stocks(warehouse_id);
CREATE INDEX idx_product_stocks_user_id ON product_stocks(user_id);

-- Добавление внешнего ключа для product_stocks
ALTER TABLE product_stocks 
ADD CONSTRAINT fk_product_stocks_warehouse 
FOREIGN KEY (warehouse_id) REFERENCES warehouses(id) ON DELETE CASCADE;

ALTER TABLE product_stocks 
ADD CONSTRAINT fk_product_stocks_product 
FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE;
