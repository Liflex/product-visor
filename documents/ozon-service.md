## Ozon Service: правила и соглашения

См. также `documents/common-rules.md` для общих принципов. Ниже — специфичные правила для сервиса.

### Назначение
Интеграции с Ozon API: синхронизация остатков, заказов, плановые задачи (scheduler), хранение чекпоинтов.

### Слои и интеграции
- Контроллеры: `OzonController`, `SyncController`, `CompanyCredentialsController`.
- Интеграция: Feign-клиенты `OzonApi`, `OzonSellerApi` с конфигом `FeignOzonConfig`.
- Планировщики: `OzonScheduler`, `OzonBackfillScheduler`.
- Kafka: консьюмеры `StockChangeConsumer`, `StockSyncConsumer`.

### Безопасность
- Доступ по JWT; для внешних запросов использовать проверку owner/company при доступе к кредам компании.
- Хранение секретов: API ключи шифруются с помощью `CryptoStringConverter` (AES/GCM).
- Переменная окружения `OZON_CRYPT_KEY` для ключа шифрования (32 символа).

### Надежность
- Retry-политика в `OzonRetryService` и исключения `OzonApiException`.
- Метрики: `OzonMetrics` (Prometheus).

### Данные
- `SyncCheckpoint` — хранение прогресса синхронизаций.

### Миграции
- Все изменения схемы через Flyway (V1..V3).


