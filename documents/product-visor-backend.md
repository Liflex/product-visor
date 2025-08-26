## Product Visor Backend: правила и соглашения

См. также `documents/common-rules.md` для общих принципов. Ниже — специфичные правила для сервиса.

### Продукты и каскады
- `Product.productStocks` – cascade = ALL, orphanRemoval = true. В `@PrePersist/@PreUpdate` выставлять `stock.setProduct(this)` и наследовать `ownerUserId` если null.
- При создании продукта допускается каскадное сохранение `productStocks` из DTO.
- Уникальность: артикул (`article`) генерируется автоматически при отсутствии.

### DTO
- Использовать `ProductDto`, `ProductStockDto`, `ProductMarketDto`, `ProductAttributeValueDto` и т.д.
- Изображения: поле `image` (byte[]), `imageUrl` для обратной совместимости.

### Склад и остатки
- Обновление остатков через `ProductStockService` с проверкой прав по product.ownerUserId и warehouse.userId.
- Синхронизация остатков – через сервис `StockSyncService` и Kafka-сообщения, с расстановкой статусов `SYNCING/UPDATED`.

### История изменений
- Любое изменение количества фиксируется через `ProductHistoryInterceptor.trackQuantityChange`.

### Миграции
- Все изменения схемы – только через Flyway (`product-visor-backend/src/main/resources/db/migration`).

### Логирование и ошибки
- Info: создание/обновление сущностей. Debug: данные DTO (без чувствительных полей). Error: исключения.


