-- Создание ProductStock записей для существующих продуктов
-- warehouse_id нужно будет проставить вручную после создания складов

INSERT INTO product_stocks (product_id, warehouse_id, user_id, quantity, sync_status, notes)
SELECT 
    p.id as product_id,
    '50a5cbff-d6b9-4635-bce0-9ee1338f8246' as warehouse_id, -- Будет проставлено вручную
    p.owner_user_id as user_id,
    COALESCE(p.quantity, 0) as quantity,
    'NEVER_SYNCED' as sync_status,
    'Автоматически создано при миграции' as notes
FROM product p
WHERE p.owner_user_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM product_stocks ps 
      WHERE ps.product_id = p.id
  );

-- Комментарий для разработчика:
-- После выполнения этой миграции необходимо вручную проставить warehouse_id
-- для созданных записей product_stocks, связав их с соответствующими складами

