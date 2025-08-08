-- Включаем расширение триграмм (pg_trgm), чтобы поддержать быстрый частичный/fuzzy-поиск по тексту
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Таблица индекса полнотекстового поиска.
-- Хранит “карту” продукта (searchable_text), собранную из всех важных полей и атрибутов.
CREATE TABLE IF NOT EXISTS visor.product_search_index (
                                                          product_id BIGINT PRIMARY KEY REFERENCES visor.product(id) ON DELETE CASCADE,
                                                          searchable_text TEXT NOT NULL
);

-- ФУНКЦИЯ: visor.compose_product_search_text(p_id)
-- ЧТО ДЕЛАЕТ: собирает “карту” продукта в один текст с учетом:
--   - name, article, barcode, category.name
--   - характеристик упаковки (width/height/length/weight/quantity_in_package)
--   - всех атрибутов (значение + имя атрибута и русское имя)
-- ЗАЧЕМ: это унифицированный текст, по которому работают триграммы и ILIKE для полного/частичного поиска.
CREATE OR REPLACE FUNCTION visor.compose_product_search_text(p_id BIGINT)
    RETURNS TEXT AS $$
DECLARE
    v_text TEXT;
BEGIN
    SELECT COALESCE(p.name,'') || ' ' ||
           COALESCE(p.article,'') || ' ' ||
           COALESCE(p.barcode,'') || ' ' ||
           COALESCE(c.name,'') || ' ' ||
           COALESCE(('w:'||p.width||' h:'||p.height||' l:'||p.length||' weight:'||p.weight||' qty_in_pack:'||p.quantity_in_package),'') || ' ' ||
           COALESCE(STRING_AGG(pa.value || ' ' || COALESCE(a.name,'') || ' ' || COALESCE(a.name_rus,''), ' '), '')
    INTO v_text
    FROM visor.product p
             LEFT JOIN visor.category c ON c.id = p.category_id
             LEFT JOIN visor.product_attribute_value pa ON pa.product_id = p.id
             LEFT JOIN visor.attribute a ON a.id = pa.attribute_id
    WHERE p.id = p_id
    GROUP BY p.id, c.name, p.width, p.height, p.length, p.weight, p.quantity_in_package;

    RETURN trim(regexp_replace(COALESCE(v_text,''), '\s+', ' ', 'g'));
END;
$$ LANGUAGE plpgsql;

-- ФУНКЦИЯ: visor.refresh_product_search_index(p_id)
-- ЧТО ДЕЛАЕТ: пересобирает текстовую “карту” и UPSERT-ит строку в product_search_index.
-- ЗАЧЕМ: централизованный механизм обновления индекса для одного товара, используется триггерами и первичной загрузкой.
CREATE OR REPLACE FUNCTION visor.refresh_product_search_index(p_id BIGINT)
    RETURNS VOID AS $$
DECLARE
    v_text TEXT;
BEGIN
    v_text := visor.compose_product_search_text(p_id);
    INSERT INTO visor.product_search_index(product_id, searchable_text)
    VALUES (p_id, v_text)
    ON CONFLICT (product_id) DO UPDATE SET searchable_text = EXCLUDED.searchable_text;
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
-- ЗАЧЕМ: чтобы изменения в атрибутах немедленно попадали в “карту” поиска.
CREATE OR REPLACE FUNCTION visor.trg_product_attr_refresh() RETURNS TRIGGER AS $$
BEGIN
    PERFORM visor.refresh_product_search_index(COALESCE(NEW.product_id, OLD.product_id));
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

-- ИНДЕКС: GIN + триграммы на агрегированном тексте
-- ЗАЧЕМ: ускоряет ILIKE и similarity-поиск по “карте” продукта
CREATE INDEX IF NOT EXISTS idx_product_search_trgm
    ON visor.product_search_index USING gin (searchable_text gin_trgm_ops);

-- ПЕРВИЧНАЯ ЗАГРУЗКА ИНДЕКСА: пройтись по всем текущим товарам и построить “карты”
DO $$
    DECLARE r RECORD;
    BEGIN
        FOR r IN SELECT id FROM visor.product LOOP
                PERFORM visor.refresh_product_search_index(r.id);
            END LOOP;
    END$$;