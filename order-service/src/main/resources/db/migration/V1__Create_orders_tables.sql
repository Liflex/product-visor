CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    posting_number VARCHAR(100) NOT NULL UNIQUE,
    source VARCHAR(50),
    status VARCHAR(50),
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    customer_name VARCHAR(255),
    customer_phone VARCHAR(50),
    address TEXT,
    total_price NUMERIC(19,2)
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT,
    offer_id VARCHAR(255),
    name VARCHAR(255),
    quantity INT,
    price NUMERIC(19,2)
);

CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);


