# Yandex Service

Сервис для работы с Yandex Market API. Реализует интеграцию с Yandex Market для получения заказов, управления остатками и синхронизации данных.

## Особенности

- **Интеграция с Yandex Market API** - полная поддержка API Yandex Market
- **Автоматическая синхронизация** - периодическая синхронизация заказов
- **Retry логика** - надежная обработка ошибок API
- **Метрики** - встроенная система мониторинга
- **Масштабируемость** - поддержка множественных компаний

## API Endpoints

### Заказы
- `POST /api/yandex/orders/fbo/list` - получение списка заказов
- `POST /api/yandex/orders/fbo/backfill` - загрузка исторических данных заказов
- `POST /api/yandex/orders/fbs/list` - получение списка заказов FBS
- `POST /api/yandex/orders/fbs/get` - получение информации о заказе
- `POST /api/yandex/orders/fbs/test` - тестовый запрос для FBS API

### Остатки
- `POST /api/yandex/stocks/update` - обновление остатков товара

### Синхронизация
- `GET /api/yandex/orders/sync-status` - статус синхронизации
- `POST /api/yandex/orders/force-sync-all-companies` - принудительная синхронизация

## Конфигурация

### Переменные окружения

```bash
# Yandex Market API
YANDEX_CLIENT_ID=your-client-id
YANDEX_CLIENT_SECRET=your-client-secret
YANDEX_DEFAULT_WAREHOUSE_ID=your-warehouse-id

# База данных
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/yandex_service
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
```

### Настройки приложения

```yaml
yandex:
  base-url: https://api.partner.market.yandex.ru
  client-id: ${YANDEX_CLIENT_ID}
  client-secret: ${YANDEX_CLIENT_SECRET}
  default-warehouse-id: ${YANDEX_DEFAULT_WAREHOUSE_ID}
  max-retry-attempts: 5
  retry-delay-ms: 1000
  retry-multiplier: 2.0

app:
  sync:
    enabled: true
    checkpoint-name: YANDEX_ORDERS
    max-gap-minutes: 60
    interval-minutes: 30
```

## Запуск

### Локально
```bash
mvn spring-boot:run -pl yandex-service
```

### Docker
```bash
docker build -t yandex-service .
docker run -p 9091:9091 yandex-service
```

## Архитектура

Сервис построен на основе библиотеки `library-marketplace` и включает:

- **YandexController** - REST API контроллер
- **YandexService** - бизнес-логика
- **YandexApi** - интеграция с Yandex Market API
- **YandexScheduler** - планировщик синхронизации
- **YandexOrderMapper** - маппинг данных заказов
- **YandexRetryService** - обработка ошибок и retry

## Мониторинг

Сервис предоставляет метрики через Actuator:
- `/actuator/health` - состояние сервиса
- `/actuator/metrics` - метрики производительности
- `/actuator/prometheus` - метрики для Prometheus

## Логирование

Логи включают:
- Информацию о синхронизации заказов
- Ошибки API и их обработку
- Метрики производительности
- Отладочную информацию

## Разработка

### Добавление новых методов API

1. Добавить метод в `YandexMarketApi`
2. Реализовать логику в `YandexService`
3. Добавить endpoint в `YandexController`
4. Обновить документацию

### Тестирование

```bash
# Запуск тестов
mvn test -pl yandex-service

# Интеграционные тесты
mvn verify -pl yandex-service
```
