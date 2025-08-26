## Yandex Service: правила и соглашения

См. также `documents/common-rules.md` для общих принципов. Ниже — специфичные правила для сервиса.

### Назначение
Интеграции с Yandex Market API: синхронизация заказов/остатков, плановые задачи, хранение кредов.

### Слои и интеграции
- Контроллеры: `YandexController`, `YandexCredentialsController`.
- Интеграция: Feign-клиенты `YandexApi`, `YandexMarketApi`, перехватчик `YandexAuthInterceptor`.
- Планировщики: `YandexScheduler`, `YandexBackfillScheduler`.
- Kafka: `StockSyncConsumer`, `OrderSyncConsumer`.

### Безопасность
- Проверять права доступа к creds по owner/company.
- Retry и fault tolerance через `YandexRetryService/Policy`.

### Миграции
- Все изменения схемы через Flyway (V1..V2…).


