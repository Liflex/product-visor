-- Добавление новых полей в таблицу products
-- Выполните этот скрипт для обновления структуры базы данных

-- Добавляем поле article (артикул) - уникальное, обязательное
ALTER TABLE products ADD COLUMN article VARCHAR(255) UNIQUE NOT NULL;

-- Добавляем поле price (цена) - обязательное, если его еще нет
ALTER TABLE products ADD COLUMN price DOUBLE PRECISION NOT NULL DEFAULT 0.0;

-- Добавляем поля для упаковки (необязательные)
ALTER TABLE products ADD COLUMN package_width DOUBLE PRECISION;
ALTER TABLE products ADD COLUMN package_height DOUBLE PRECISION;
ALTER TABLE products ADD COLUMN package_length DOUBLE PRECISION;
ALTER TABLE products ADD COLUMN package_weight DOUBLE PRECISION;
ALTER TABLE products ADD COLUMN package_quantity_in_package INTEGER;

-- Создаем индекс для быстрого поиска по артикулу
CREATE INDEX idx_products_article ON products(article);

-- Обновляем существующие записи, генерируя артикулы для товаров без артикула
UPDATE products 
SET article = CONCAT('ART', LPAD(id::text, 9, '0'))
WHERE article IS NULL OR article = '';

-- Комментарии к полям
COMMENT ON COLUMN products.article IS 'Уникальный артикул товара';
COMMENT ON COLUMN products.price IS 'Цена товара';
COMMENT ON COLUMN products.package_width IS 'Ширина упаковки в см';
COMMENT ON COLUMN products.package_height IS 'Высота упаковки в см';
COMMENT ON COLUMN products.package_length IS 'Длина упаковки в см';
COMMENT ON COLUMN products.package_weight IS 'Вес упаковки в кг';
COMMENT ON COLUMN products.package_quantity_in_package IS 'Количество товаров в упаковке'; 