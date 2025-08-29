-- Миграция существующего поискового индекса к новой структуре
-- Выполняется после применения product_search_setup_v2.sql

-- 1. Добавляем новые колонки к существующей таблице
ALTER TABLE visor.product_search_index 
ADD COLUMN IF NOT EXISTS stock_info TEXT,
ADD COLUMN IF NOT EXISTS warehouse_names TEXT;

-- 2. Удаляем старые триггеры (если они существуют)
DROP TRIGGER IF EXISTS product_refresh_search ON visor.product;
DROP TRIGGER IF EXISTS product_attr_refresh_search ON visor.product_attribute_value;

-- 3. Удаляем старые функции (будут пересозданы новой версией)
DROP FUNCTION IF EXISTS visor.compose_product_search_text(BIGINT);
DROP FUNCTION IF EXISTS visor.refresh_product_search_index(BIGINT);
DROP FUNCTION IF EXISTS visor.trg_product_refresh();
DROP FUNCTION IF EXISTS visor.trg_product_attr_refresh();

-- 4. Удаляем старые индексы (будут пересозданы)
DROP INDEX IF EXISTS visor.idx_product_search_trgm;

-- 5. Обновляем существующие записи в индексе
-- Пересобираем все существующие записи с новой структурой
DO $$
    DECLARE r RECORD;
    BEGIN
        FOR r IN SELECT id FROM visor.product LOOP
                PERFORM visor.refresh_product_search_index(r.id);
            END LOOP;
    END$$;

-- 6. Создаем новые индексы
CREATE INDEX IF NOT EXISTS idx_product_search_trgm
    ON visor.product_search_index USING gin (searchable_text gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_product_search_warehouse_trgm
    ON visor.product_search_index USING gin (warehouse_names gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_product_search_stock_trgm
    ON visor.product_search_index USING gin (stock_info gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_product_search_combined_trgm
    ON visor.product_search_index USING gin ((searchable_text || ' ' || COALESCE(stock_info,'') || ' ' || COALESCE(warehouse_names,'')) gin_trgm_ops);

-- 7. Создаем новые триггеры
CREATE TRIGGER product_refresh_search
    AFTER INSERT OR UPDATE ON visor.product
    FOR EACH ROW EXECUTE FUNCTION visor.trg_product_refresh();

CREATE TRIGGER product_attr_refresh_search
    AFTER INSERT OR UPDATE OR DELETE ON visor.product_attribute_value
    FOR EACH ROW EXECUTE FUNCTION visor.trg_product_attr_refresh();

CREATE TRIGGER product_stock_refresh_search
    AFTER INSERT OR UPDATE OR DELETE ON visor.product_stocks
    FOR EACH ROW EXECUTE FUNCTION visor.trg_product_stock_refresh();

CREATE TRIGGER warehouse_refresh_search
    AFTER UPDATE ON visor.warehouses
    FOR EACH ROW EXECUTE FUNCTION visor.trg_warehouse_refresh();

CREATE TRIGGER product_stock_warehouse_refresh_search
    AFTER INSERT OR DELETE ON visor.product_stock_warehouses
    FOR EACH ROW EXECUTE FUNCTION visor.trg_product_stock_warehouse_refresh();

-- 8. Проверяем результат
SELECT 
    'Migration completed. Total products indexed: ' || COUNT(*) as migration_status
FROM visor.product_search_index;

-- 9. Показываем пример поиска
SELECT 'Example search results for "test":' as info;
SELECT product_id, similarity, LEFT(searchable_text, 100) as preview
FROM visor.search_products('test', 5);
