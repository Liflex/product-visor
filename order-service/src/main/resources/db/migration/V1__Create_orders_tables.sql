-- Создаем схему orders если она не существует
CREATE SCHEMA IF NOT EXISTS orders;

-- Создаем таблицу заказов
CREATE TABLE IF NOT EXISTS orders.orders (
    id BIGSERIAL PRIMARY KEY,
    posting_number VARCHAR(100) NOT NULL UNIQUE,
    source VARCHAR(50),
    market VARCHAR(50),
    status VARCHAR(50),
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    customer_name VARCHAR(255),
    customer_phone VARCHAR(50),
    address TEXT,
    total_price NUMERIC(19,2)
);

-- Создаем таблицу товаров заказа
CREATE TABLE IF NOT EXISTS orders.order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders.orders(id) ON DELETE CASCADE,
    product_id BIGINT,
    offer_id VARCHAR(255),
    name VARCHAR(255),
    quantity INT,
    price NUMERIC(19,2)
);

-- Создаем индексы
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders.orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders.orders(created_at);
CREATE INDEX IF NOT EXISTS idx_orders_market ON orders.orders(market);





