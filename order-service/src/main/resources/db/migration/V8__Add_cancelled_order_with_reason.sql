-- Добавление тестового отмененного заказа с причиной отмены для проверки кнопки "Причина"

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
    total_price,
    cancel_reason,
    cancel_reason_id,
    cancellation_type
) VALUES (
    'CANCELLED-TEST-001',
    'OZON_FBS',
    'OZON',
    'CANCELLED',
    '2024-01-20T10:00:00+03:00'::timestamptz,
    '2024-01-20T11:30:00+03:00'::timestamptz,
    'Тестовый Клиент Отмены',
    '+7 (999) 999-99-99',
    'г. Москва, ул. Тестовая, д. 1, кв. 1',
    2500.00,
    'Товар отсутствует на складе. Клиент отказался от замены на аналогичный товар.',
    123,
    'CLIENT_CANCELLED'
) ON CONFLICT (posting_number) DO NOTHING;

-- Еще один тестовый отмененный заказ
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
    total_price,
    cancel_reason,
    cancel_reason_id,
    cancellation_type
) VALUES (
    'CANCELLED-TEST-002',
    'WILDBERRIES_API',
    'WILDBERRIES',
    'CANCELLED',
    '2024-01-21T14:20:00+03:00'::timestamptz,
    '2024-01-21T15:45:00+03:00'::timestamptz,
    'Другой Тестовый Клиент',
    '+7 (999) 888-88-88',
    'г. Санкт-Петербург, ул. Другая, д. 2, кв. 2',
    1800.50,
    'Клиент изменил адрес доставки, но новый адрес находится вне зоны доставки.',
    456,
    'DELIVERY_UNAVAILABLE'
) ON CONFLICT (posting_number) DO NOTHING;

