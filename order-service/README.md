# Order Service

Сервис для управления заказами в системе Product Visor.

## Настройка базы данных

### 1. Создание схемы

Order Service использует отдельную схему `orders` в базе данных PostgreSQL. Схема создается автоматически при запуске приложения через Flyway.

### 2. Конфигурация

Основные настройки в `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/product_visor
    username: postgres
    password: postgres
    hikari:
      schema: orders
  jpa:
    properties:
      hibernate:
        default_schema: orders
  flyway:
    enabled: true
    locations: classpath:db/migration
    schemas: orders
    default-schema: orders
    baseline-on-migrate: true
    validate-on-migrate: true
```

### 3. Миграции

Миграции находятся в `src/main/resources/db/migration/`:

- `V1__Create_orders_tables.sql` - создание схемы и основных таблиц
- `V2__Add_market_column.sql` - добавление поля market
- `V3__Insert_test_orders.sql` - вставка тестовых заказов для проверки интеграции

### 4. Структура таблиц

#### orders.orders
- `id` - уникальный идентификатор
- `posting_number` - номер постинга (уникальный)
- `source` - источник заказа
- `market` - маркет (enum: OZON, WILDBERRIES, YANDEX_MARKET, ALIEXPRESS, OTHER)
- `status` - статус заказа
- `created_at` - дата создания
- `updated_at` - дата обновления
- `customer_name` - имя клиента
- `customer_phone` - телефон клиента
- `address` - адрес доставки
- `total_price` - общая сумма заказа

#### orders.order_items
- `id` - уникальный идентификатор
- `order_id` - ссылка на заказ
- `product_id` - ID товара
- `offer_id` - ID предложения
- `name` - название товара
- `quantity` - количество
- `price` - цена

### 5. Тестовые данные

При первом запуске автоматически создаются тестовые заказы для проверки интеграции:

#### Заказы по маркетам:
- **Ozon**: 1 заказ (COMPLETED) - контактные линзы Acuvue Oasys 1-Day
- **Wildberries**: 1 заказ (PROCESSING) - раствор для линз + контейнер
- **Yandex Market**: 1 заказ (SHIPPED) - контактные линзы Air Optix
- **AliExpress**: 1 заказ (DELIVERED) - цветные контактные линзы
- **Other**: 1 заказ (CANCELLED) - контактные линзы Biofinity

#### Тестовые номера заказов:
- `OZON-001-2024-01-15`
- `WB-002-2024-01-16`
- `YM-003-2024-01-17`
- `AE-004-2024-01-18`
- `OTHER-005-2024-01-19`

### 6. Запуск

```bash
# Сборка
mvn clean compile

# Запуск
mvn spring-boot:run
```

### 7. API Endpoints

- `GET /api/orders` - список всех заказов с пагинацией
- `GET /api/orders/market/{market}` - заказы по маркету
- `GET /api/orders/{postingNumber}` - заказ по номеру постинга

### 8. gRPC

Сервис предоставляет gRPC endpoint для интеграции с другими сервисами:
- `OrderInternalService.upsertOrders` - пакетное обновление заказов

### 9. Мониторинг

- Health check: `GET /actuator/health`
- Prometheus metrics: `GET /actuator/prometheus`
