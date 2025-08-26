-- Добавление поля stock_type в таблицу product_stocks
ALTER TABLE product_stocks 
ADD COLUMN stock_type VARCHAR(20) NOT NULL DEFAULT 'FBS' 
CHECK (stock_type IN ('FBS', 'YANDEX_FBO', 'OZON_FBO'));

-- Создание индекса для stock_type
CREATE INDEX idx_product_stocks_stock_type ON product_stocks(stock_type);

