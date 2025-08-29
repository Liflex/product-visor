-- Включаем расширение триграмм (pg_trgm), чтобы поддержать быстрый частичный/fuzzy-поиск по тексту
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Таблица индекса полнотекстового поиска.
-- Хранит "карту" продукта (searchable_text), собранную из всех важных полей и атрибутов.
-- Обновлена для включения информации о складах и остатках
CREATE TABLE IF NOT EXISTS visor.product_search_index (
    product_id BIGINT PRIMARY KEY REFERENCES visor.product(id) ON DELETE CASCADE,
    searchable_text TEXT NOT NULL,
    stock_info TEXT, -- информация о складах и остатках
    warehouse_names TEXT -- названия складов для поиска
);

-- ФУНКЦИЯ: visor.compose_product_search_text(p_id)
-- ЧТО ДЕЛАЕТ: собирает "карту" продукта в один текст с учетом:
--   - name, article, barcode, category.name
--   - характеристик упаковки (width/height/length/weight/quantity_in_package)
--   - всех атрибутов (значение + имя атрибута и русское имя)
--   - информации о складах и остатках
-- ЗАЧЕМ: это унифицированный текст, по которому работают триграммы и ILIKE для полного/частичного поиска.
CREATE OR REPLACE FUNCTION visor.compose_product_search_text(p_id BIGINT)
    RETURNS TEXT AS $$
DECLARE
    v_text TEXT;
    v_package_info TEXT;
BEGIN
    -- Собираем основную информацию о продукте
    SELECT COALESCE(p.name,'') || ' ' ||
           COALESCE(p.article,'') || ' ' ||
           COALESCE(p.barcode,'') || ' ' ||
           COALESCE(c.name,'') || ' ' ||
           COALESCE(STRING_AGG(pa.value || ' ' || COALESCE(a.name,'') || ' ' || COALESCE(a.name_rus,''), ' '), '')
    INTO v_text
    FROM visor.product p
             LEFT JOIN visor.category c ON c.id = p.category_id
             LEFT JOIN visor.product_attribute_value pa ON pa.product_id = p.id
             LEFT JOIN visor.attribute a ON a.id = pa.attribute_id
    WHERE p.id = p_id
    GROUP BY p.id, c.name;

    -- Добавляем информацию об упаковке из embedded объекта
    SELECT COALESCE(('w:'||p.width||' h:'||p.height||' l:'||p.length||' weight:'||p.weight||' qty_in_pack:'||p.quantity_in_package),'')
    INTO v_package_info
    FROM visor.product p
    WHERE p.id = p_id;

    -- Объединяем основную информацию и информацию об упаковке
    v_text := COALESCE(v_text,'') || ' ' || COALESCE(v_package_info,'');

    RETURN trim(regexp_replace(COALESCE(v_text,''), '\s+', ' ', 'g'));
END;
$$ LANGUAGE plpgsql;

-- ФУНКЦИЯ: visor.compose_stock_info(p_id)
-- ЧТО ДЕЛАЕТ: собирает информацию о складах и остатках для продукта
CREATE OR REPLACE FUNCTION visor.compose_stock_info(p_id BIGINT)
    RETURNS TEXT AS $$
DECLARE
    v_stock_info TEXT;
    v_warehouse_names TEXT;
BEGIN
    -- Собираем информацию о складах и остатках
    SELECT COALESCE(STRING_AGG(
        'stock:' || ps.stock_type || ' qty:' || ps.quantity || ' sync:' || ps.sync_status, ' '
    ), '')
    INTO v_stock_info
    FROM visor.product_stocks ps
    WHERE ps.product_id = p_id;

    -- Собираем названия складов
    SELECT COALESCE(STRING_AGG(DISTINCT w.name, ' '), '')
    INTO v_warehouse_names
    FROM visor.product_stocks ps
             JOIN visor.product_stock_warehouses psw ON psw.product_stock_id = ps.id
             JOIN visor.warehouses w ON w.id = psw.warehouse_id
    WHERE ps.product_id = p_id AND w.is_active = true;

    RETURN COALESCE(v_stock_info,'') || ' ' || COALESCE(v_warehouse_names,'');
END;
$$ LANGUAGE plpgsql;

-- ФУНКЦИЯ: visor.refresh_product_search_index(p_id)
-- ЧТО ДЕЛАЕТ: пересобирает текстовую "карту" и UPSERT-ит строку в product_search_index.
-- ЗАЧЕМ: централизованный механизм обновления индекса для одного товара, используется триггерами и первичной загрузкой.
CREATE OR REPLACE FUNCTION visor.refresh_product_search_index(p_id BIGINT)
    RETURNS VOID AS $$
DECLARE
    v_text TEXT;
    v_stock_info TEXT;
    v_warehouse_names TEXT;
BEGIN
    v_text := visor.compose_product_search_text(p_id);
    v_stock_info := visor.compose_stock_info(p_id);
    
    -- Извлекаем только названия складов для отдельного поиска
    SELECT COALESCE(STRING_AGG(DISTINCT w.name, ' '), '')
    INTO v_warehouse_names
    FROM visor.product_stocks ps
             JOIN visor.product_stock_warehouses psw ON psw.product_stock_id = ps.id
             JOIN visor.warehouses w ON w.id = psw.warehouse_id
    WHERE ps.product_id = p_id AND w.is_active = true;

    INSERT INTO visor.product_search_index(product_id, searchable_text, stock_info, warehouse_names)
    VALUES (p_id, v_text, v_stock_info, v_warehouse_names)
    ON CONFLICT (product_id) DO UPDATE SET 
        searchable_text = EXCLUDED.searchable_text,
        stock_info = EXCLUDED.stock_info,
        warehouse_names = EXCLUDED.warehouse_names;
END;
$$ LANGUAGE plpgsql;

-- ТРИГГЕР-ФУНКЦИЯ: visor.trg_product_refresh()
-- ЧТО ДЕЛАЕТ: при INSERT/UPDATE строки в product пересобирает индекс для этого товара.
-- ЗАЧЕМ: чтобы индекс всегда был актуален при изменении полей самого товара.
CREATE OR REPLACE FUNCTION visor.trg_product_refresh() RETURNS TRIGGER AS $$
BEGIN
    PERFORM visor.refresh_product_search_index(NEW.id);
    RETURN NEW;
END; $$ LANGUAGE plpgsql;

-- ТРИГГЕР-ФУНКЦИЯ: visor.trg_product_attr_refresh()
-- ЧТО ДЕЛАЕТ: при INSERT/UPDATE/DELETE значений атрибутов пересобирает индекс для связанного товара.
-- ЗАЧЕМ: чтобы изменения в атрибутах немедленно попадали в "карту" поиска.
CREATE OR REPLACE FUNCTION visor.trg_product_attr_refresh() RETURNS TRIGGER AS $$
BEGIN
    PERFORM visor.refresh_product_search_index(COALESCE(NEW.product_id, OLD.product_id));
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;
    RETURN NEW;
END; $$ LANGUAGE plpgsql;

-- ТРИГГЕР-ФУНКЦИЯ: visor.trg_product_stock_refresh()
-- ЧТО ДЕЛАЕТ: при INSERT/UPDATE/DELETE остатков пересобирает индекс для связанного товара.
-- ЗАЧЕМ: чтобы изменения в остатках и складах немедленно попадали в "карту" поиска.
CREATE OR REPLACE FUNCTION visor.trg_product_stock_refresh() RETURNS TRIGGER AS $$
BEGIN
    PERFORM visor.refresh_product_search_index(COALESCE(NEW.product_id, OLD.product_id));
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;
    RETURN NEW;
END; $$ LANGUAGE plpgsql;

-- ТРИГГЕР-ФУНКЦИЯ: visor.trg_warehouse_refresh()
-- ЧТО ДЕЛАЕТ: при UPDATE склада пересобирает индексы для всех связанных товаров.
-- ЗАЧЕМ: чтобы изменения в названиях складов немедленно попадали в поиск.
CREATE OR REPLACE FUNCTION visor.trg_warehouse_refresh() RETURNS TRIGGER AS $$
DECLARE
    r RECORD;
BEGIN
    -- Если изменилось название склада, обновляем индексы всех связанных товаров
    IF OLD.name != NEW.name OR OLD.is_active != NEW.is_active THEN
        FOR r IN 
            SELECT DISTINCT ps.product_id
            FROM visor.product_stocks ps
                     JOIN visor.product_stock_warehouses psw ON psw.product_stock_id = ps.id
            WHERE psw.warehouse_id = NEW.id
        LOOP
            PERFORM visor.refresh_product_search_index(r.product_id);
        END LOOP;
    END IF;
    RETURN NEW;
END; $$ LANGUAGE plpgsql;

-- ТРИГГЕР-ФУНКЦИЯ: visor.trg_product_stock_warehouse_refresh()
-- ЧТО ДЕЛАЕТ: при INSERT/DELETE связей товар-склад пересобирает индекс для товара.
-- ЗАЧЕМ: чтобы изменения в привязках к складам немедленно попадали в поиск.
CREATE OR REPLACE FUNCTION visor.trg_product_stock_warehouse_refresh() RETURNS TRIGGER AS $$
DECLARE
    v_product_id BIGINT;
BEGIN
    -- Получаем product_id из product_stocks
    SELECT ps.product_id INTO v_product_id
    FROM visor.product_stocks ps
    WHERE ps.id = COALESCE(NEW.product_stock_id, OLD.product_stock_id);
    
    IF v_product_id IS NOT NULL THEN
        PERFORM visor.refresh_product_search_index(v_product_id);
    END IF;
    
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;
    RETURN NEW;
END; $$ LANGUAGE plpgsql;

-- ТРИГГЕРЫ на таблицы — подключают функции выше

-- Триггер на продукт: обновляет индекс при вставке/изменении товара
DROP TRIGGER IF EXISTS product_refresh_search ON visor.product;
CREATE TRIGGER product_refresh_search
    AFTER INSERT OR UPDATE ON visor.product
    FOR EACH ROW EXECUTE FUNCTION visor.trg_product_refresh();

-- Триггер на значения атрибутов: обновляет индекс при любом изменении атрибутов товара
DROP TRIGGER IF EXISTS product_attr_refresh_search ON visor.product_attribute_value;
CREATE TRIGGER product_attr_refresh_search
    AFTER INSERT OR UPDATE OR DELETE ON visor.product_attribute_value
    FOR EACH ROW EXECUTE FUNCTION visor.trg_product_attr_refresh();

-- Триггер на остатки: обновляет индекс при изменении остатков
DROP TRIGGER IF EXISTS product_stock_refresh_search ON visor.product_stocks;
CREATE TRIGGER product_stock_refresh_search
    AFTER INSERT OR UPDATE OR DELETE ON visor.product_stocks
    FOR EACH ROW EXECUTE FUNCTION visor.trg_product_stock_refresh();

-- Триггер на склады: обновляет индексы при изменении названий складов
DROP TRIGGER IF EXISTS warehouse_refresh_search ON visor.warehouses;
CREATE TRIGGER warehouse_refresh_search
    AFTER UPDATE ON visor.warehouses
    FOR EACH ROW EXECUTE FUNCTION visor.trg_warehouse_refresh();

-- Триггер на связи товар-склад: обновляет индекс при изменении привязок
DROP TRIGGER IF EXISTS product_stock_warehouse_refresh_search ON visor.product_stock_warehouses;
CREATE TRIGGER product_stock_warehouse_refresh_search
    AFTER INSERT OR DELETE ON visor.product_stock_warehouses
    FOR EACH ROW EXECUTE FUNCTION visor.trg_product_stock_warehouse_refresh();

-- ИНДЕКСЫ: GIN + триграммы на агрегированном тексте
-- ЗАЧЕМ: ускоряет ILIKE и similarity-поиск по "карте" продукта
CREATE INDEX IF NOT EXISTS idx_product_search_trgm
    ON visor.product_search_index USING gin (searchable_text public.gin_trgm_ops);

-- Индекс для поиска по складам
CREATE INDEX IF NOT EXISTS idx_product_search_warehouse_trgm
    ON visor.product_search_index USING gin (warehouse_names public.gin_trgm_ops);

-- Индекс для поиска по информации об остатках
CREATE INDEX IF NOT EXISTS idx_product_search_stock_trgm
    ON visor.product_search_index USING gin (stock_info public.gin_trgm_ops);

-- Комбинированный индекс для поиска по всем полям
CREATE INDEX IF NOT EXISTS idx_product_search_combined_trgm
    ON visor.product_search_index USING gin ((searchable_text || ' ' || COALESCE(stock_info,'') || ' ' || COALESCE(warehouse_names,'')) public.gin_trgm_ops);

-- ПЕРВИЧНАЯ ЗАГРУЗКА ИНДЕКСА: пройтись по всем текущим товарам и построить "карты"
DO $$
    DECLARE r RECORD;
    BEGIN
        FOR r IN SELECT id FROM visor.product LOOP
                PERFORM visor.refresh_product_search_index(r.id);
            END LOOP;
    END$$;

-- ФУНКЦИЯ ДЛЯ ПОИСКА: visor.search_products(search_query, limit_count)
-- ЧТО ДЕЛАЕТ: ищет товары по текстовому запросу с учетом всех полей
-- ЗАЧЕМ: унифицированный поиск по товарам с учетом складов и остатков
CREATE OR REPLACE FUNCTION visor.search_products(search_query TEXT, limit_count INTEGER DEFAULT 50)
    RETURNS TABLE(
                     product_id BIGINT,
                     similarity REAL,
                     searchable_text TEXT,
                     stock_info TEXT,
                     warehouse_names TEXT
                 ) AS $$
BEGIN
    RETURN QUERY
        SELECT
            psi.product_id,
            GREATEST(
                    public.similarity(psi.searchable_text, search_query),
                    public.similarity(COALESCE(psi.stock_info, ''), search_query),
                    public.similarity(COALESCE(psi.warehouse_names, ''), search_query)
            ) as similarity,
            psi.searchable_text,
            psi.stock_info,
            psi.warehouse_names
        FROM visor.product_search_index psi
        WHERE
            psi.searchable_text ILIKE '%' || search_query || '%'
           OR COALESCE(psi.stock_info, '') ILIKE '%' || search_query || '%'
           OR COALESCE(psi.warehouse_names, '') ILIKE '%' || search_query || '%'
        ORDER BY similarity DESC
        LIMIT limit_count;
END;
$$ LANGUAGE plpgsql;
