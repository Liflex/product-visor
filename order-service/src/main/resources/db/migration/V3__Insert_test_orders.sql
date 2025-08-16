-- Вставка тестовых заказов для проверки интеграции

-- Тестовый заказ из Ozon
INSERT INTO orders.orders (
    posting_number, 
    source, 
    market, 
    status, 
    created_at, 
    updated_at, 
    customer_name, 
    customer_phone, 
    address, 
    total_price
) VALUES (
    'OZON-001-2024-01-15',
    'OZON_FBO',
    'OZON',
    'COMPLETED',
    '2024-01-15T10:30:00+03:00'::timestamptz,
    '2024-01-15T14:45:00+03:00'::timestamptz,
    'Иванов Иван Иванович',
    '+7 (999) 123-45-67',
    'г. Москва, ул. Тверская, д. 1, кв. 10',
    2999.00
) ON CONFLICT (posting_number) DO NOTHING;

-- Тестовый заказ из Wildberries
INSERT INTO orders.orders (
    posting_number, 
    source, 
    market, 
    status, 
    created_at, 
    updated_at, 
    customer_name, 
    customer_phone, 
    address, 
    total_price
) VALUES (
    'WB-002-2024-01-16',
    'WILDBERRIES_API',
    'WILDBERRIES',
    'PROCESSING',
    '2024-01-16T09:15:00+03:00'::timestamptz,
    '2024-01-16T09:15:00+03:00'::timestamptz,
    'Петрова Анна Сергеевна',
    '+7 (999) 234-56-78',
    'г. Санкт-Петербург, пр. Невский, д. 25, кв. 5',
    1599.50
) ON CONFLICT (posting_number) DO NOTHING;

-- Тестовый заказ из Yandex Market
INSERT INTO orders.orders (
    posting_number, 
    source, 
    market, 
    status, 
    created_at, 
    updated_at, 
    customer_name, 
    customer_phone, 
    address, 
    total_price
) VALUES (
    'YM-003-2024-01-17',
    'YANDEX_MARKET_API',
    'YANDEX_MARKET',
    'SHIPPED',
    '2024-01-17T16:20:00+03:00'::timestamptz,
    '2024-01-17T18:30:00+03:00'::timestamptz,
    'Сидоров Алексей Петрович',
    '+7 (999) 345-67-89',
    'г. Екатеринбург, ул. Ленина, д. 15, кв. 22',
    4599.00
) ON CONFLICT (posting_number) DO NOTHING;

-- Тестовый заказ из AliExpress
INSERT INTO orders.orders (
    posting_number, 
    source, 
    market, 
    status, 
    created_at, 
    updated_at, 
    customer_name, 
    customer_phone, 
    address, 
    total_price
) VALUES (
    'AE-004-2024-01-18',
    'ALIEXPRESS_API',
    'ALIEXPRESS',
    'DELIVERED',
    '2024-01-18T11:45:00+03:00'::timestamptz,
    '2024-01-18T15:20:00+03:00'::timestamptz,
    'Козлова Мария Дмитриевна',
    '+7 (999) 456-78-90',
    'г. Новосибирск, ул. Красная, д. 8, кв. 12',
    899.99
) ON CONFLICT (posting_number) DO NOTHING;

-- Тестовый заказ из другого источника
INSERT INTO orders.orders (
    posting_number, 
    source, 
    market, 
    status, 
    created_at, 
    updated_at, 
    customer_name, 
    customer_phone, 
    address, 
    total_price
) VALUES (
    'OTHER-005-2024-01-19',
    'MANUAL_ENTRY',
    'OTHER',
    'CANCELLED',
    '2024-01-19T13:10:00+03:00'::timestamptz,
    '2024-01-19T14:25:00+03:00'::timestamptz,
    'Васильев Дмитрий Александрович',
    '+7 (999) 567-89-01',
    'г. Казань, ул. Баумана, д. 30, кв. 7',
    1299.00
) ON CONFLICT (posting_number) DO NOTHING;

-- Вставка тестовых товаров для заказов

-- Товары для заказа OZON-001
INSERT INTO orders.order_items (order_id, product_id, offer_id, name, quantity, price) 
SELECT o.id, 1, 'OZON-OFFER-001', 'Контактные линзы Acuvue Oasys 1-Day', 2, 1499.50
FROM orders.orders o WHERE o.posting_number = 'OZON-001-2024-01-15'
ON CONFLICT DO NOTHING;

-- Товары для заказа WB-002
INSERT INTO orders.order_items (order_id, product_id, offer_id, name, quantity, price) 
SELECT o.id, 2, 'WB-OFFER-002', 'Раствор для линз Opti-Free Puremoist', 1, 799.75
FROM orders.orders o WHERE o.posting_number = 'WB-002-2024-01-16'
ON CONFLICT DO NOTHING;

INSERT INTO orders.order_items (order_id, product_id, offer_id, name, quantity, price) 
SELECT o.id, 3, 'WB-OFFER-003', 'Контейнер для линз с зеркалом', 1, 799.75
FROM orders.orders o WHERE o.posting_number = 'WB-002-2024-01-16'
ON CONFLICT DO NOTHING;

-- Товары для заказа YM-003
INSERT INTO orders.order_items (order_id, product_id, offer_id, name, quantity, price) 
SELECT o.id, 4, 'YM-OFFER-004', 'Контактные линзы Air Optix plus HydraGlyde', 3, 1533.00
FROM orders.orders o WHERE o.posting_number = 'YM-003-2024-01-17'
ON CONFLICT DO NOTHING;

-- Товары для заказа AE-004
INSERT INTO orders.order_items (order_id, product_id, offer_id, name, quantity, price) 
SELECT o.id, 5, 'AE-OFFER-005', 'Контактные линзы ColorBlenz Natural', 1, 899.99
FROM orders.orders o WHERE o.posting_number = 'AE-004-2024-01-18'
ON CONFLICT DO NOTHING;

-- Товары для заказа OTHER-005
INSERT INTO orders.order_items (order_id, product_id, offer_id, name, quantity, price) 
SELECT o.id, 6, 'OTHER-OFFER-006', 'Контактные линзы Biofinity', 1, 1299.00
FROM orders.orders o WHERE o.posting_number = 'OTHER-005-2024-01-19'
ON CONFLICT DO NOTHING;
