# Стандартизация OzonService для поддержки множественных маркетплейсов

## Обзор

Данный документ описывает стандартизацию OzonService для подготовки к созданию YandexService и других маркетплейсов. Все компоненты были обезличены и параметризованы для минимизации дублирования кода.

## Созданные базовые классы и интерфейсы

### 1. Контроллеры
- **`BaseMarketplaceController`** - базовый контроллер с общими эндпоинтами
  - `/orders/fbo/list` - список заказов FBO
  - `/orders/fbo/backfill` - загрузка исторических данных
  - `/orders/fbs/list` - список заказов FBS
  - `/orders/fbs/get` - информация о заказе FBS
  - `/warehouses` - список складов
  - `/stock/{offerId}` - обновление остатков
  - `/sync/status` - статус синхронизации
  - `/sync/force` - принудительная синхронизация

### 2. Сервисы
- **`BaseMarketplaceService`** - интерфейс для сервисов маркетплейсов
  - `fboPostingList()` - получение списка заказов FBO
  - `backfillAllOrders()` - загрузка исторических данных
  - `fbsPostingList()` - получение списка заказов FBS
  - `fbsPostingGet()` - получение информации о заказе FBS
  - `updateStock()` - обновление остатков
  - `testConnection()` - проверка подключения

### 3. API интеграция
- **`BaseMarketplaceApi`** - интерфейс для внешних API
  - `listWarehouses()` - список складов
  - `getFboOrders()` - заказы FBO
  - `getFbsOrders()` - заказы FBS
  - `getFbsOrder()` - информация о заказе FBS
  - `updateStock()` - обновление остатков
  - `testConnection()` - проверка подключения

### 4. Планировщики
- **`BaseMarketplaceScheduler`** - интерфейс для планировщиков синхронизации
  - `forceSync()` - принудительная синхронизация
  - `getLastSyncInfo()` - информация о последней синхронизации
  - `startAutoSync()` - запуск автоматической синхронизации
  - `stopAutoSync()` - остановка автоматической синхронизации
  - `isSyncRunning()` - проверка выполнения синхронизации

### 5. Retry логика
- **`BaseMarketplaceRetryPolicy`** - универсальная политика retry
  - Настраиваемое количество попыток
  - Экспоненциальная задержка
  - Абстрактный метод `isRetryableError()` для специфичных ошибок
- **`BaseMarketplaceApiException`** - базовое исключение для API
  - Содержит код ошибки, сообщение и название маркетплейса
  - Абстрактный метод `isRetryable()` для определения повторяемости
- **`BaseMarketplaceRetryService`** - универсальный сервис retry
  - Аннотация `@Retryable` с настраиваемыми параметрами
  - Функциональный интерфейс для операций API

### 6. Конфигурация
- **`BaseMarketplaceProperties`** - базовый класс для свойств
  - Общие настройки: baseUrl, clientId, apiKey, warehouseId
  - Настройки retry: maxAttempts, delay, multiplier
  - Настройки синхронизации: interval, autoSync
  - Методы валидации и получения сводки конфигурации

### 7. Маппинг
- **`BaseMarketplaceOrderMapper`** - интерфейс для мапперов заказов
  - `mapOrderToDto()` - преобразование заказа в DTO
  - `mapOrdersToDto()` - преобразование списка заказов
  - `mapProductToItem()` - преобразование товара в OrderItemDto
  - Методы извлечения данных из JsonNode

### 8. Метрики
- **`BaseMarketplaceMetrics`** - базовый класс для метрик
  - Метрики операций: upserted, batch, errors
  - Метрики API: requests, response_time
  - Метрики синхронизации: sync, sync_duration
  - Кастомные метрики с тегами

## Как создать новый маркетплейс

### 1. Создать структуру директорий
```
yandex-service/
├── src/main/java/ru/dmitartur/yandex/
│   ├── controller/
│   ├── service/
│   ├── integration/
│   ├── scheduled/
│   ├── retry/
│   ├── config/
│   ├── mapper/
│   ├── metrics/
│   ├── dto/
│   └── YandexServiceApplication.java
```

### 2. Наследовать базовые классы

#### Контроллер
```java
@RestController
@RequestMapping("/api/yandex")
public class YandexController extends BaseMarketplaceController {
    
    public YandexController() {
        super("Yandex", "/api/yandex");
    }
    
    @Override
    protected BaseMarketplaceService getMarketplaceService() {
        return yandexService;
    }
    
    @Override
    protected BaseMarketplaceApi getMarketplaceApi() {
        return yandexApi;
    }
    
    @Override
    protected BaseMarketplaceScheduler getScheduler() {
        return yandexScheduler;
    }
}
```

#### Сервис
```java
@Service
public class YandexService implements BaseMarketplaceService {
    
    @Override
    public String getMarketplaceName() {
        return "Yandex";
    }
    
    // Реализовать все методы интерфейса
}
```

#### API
```java
@Component
public class YandexApi implements BaseMarketplaceApi {
    
    @Override
    public String getMarketplaceName() {
        return "Yandex";
    }
    
    // Реализовать все методы интерфейса
}
```

#### Планировщик
```java
@Component
public class YandexScheduler implements BaseMarketplaceScheduler {
    
    @Override
    public String getMarketplaceName() {
        return "Yandex";
    }
    
    // Реализовать все методы интерфейса
}
```

#### Retry политика
```java
@Component
public class YandexRetryPolicy extends BaseMarketplaceRetryPolicy {
    
    public YandexRetryPolicy() {
        super("Yandex", 5);
    }
    
    @Override
    protected boolean isRetryableError(String errorCode, String errorMessage) {
        // Специфичная логика для Yandex
        switch (errorCode) {
            case "RATE_LIMIT":
            case "SERVICE_UNAVAILABLE":
                return true;
            default:
                return false;
        }
    }
}
```

#### Исключения
```java
public class YandexApiException extends BaseMarketplaceApiException {
    
    public YandexApiException(String errorCode, String errorMessage) {
        super("Yandex", errorCode, errorMessage);
    }
    
    @Override
    public boolean isRetryable() {
        // Логика определения повторяемости для Yandex
        return "RATE_LIMIT".equals(getErrorCode());
    }
}
```

#### Конфигурация
```java
@Component
@ConfigurationProperties(prefix = "yandex")
public class YandexProperties extends BaseMarketplaceProperties {
    
    @Override
    public String getMarketplaceName() {
        return "Yandex";
    }
    
    // Дополнительные специфичные свойства
}
```

#### Маппер
```java
@Component
public class YandexOrderMapper implements BaseMarketplaceOrderMapper {
    
    @Override
    public String getMarketplaceName() {
        return "Yandex";
    }
    
    @Override
    public String getOrderSource() {
        return "YANDEX_FBO";
    }
    
    // Реализовать все методы интерфейса
}
```

#### Метрики
```java
@Component
public class YandexMetrics extends BaseMarketplaceMetrics {
    
    public YandexMetrics(MeterRegistry meterRegistry) {
        super(meterRegistry, "Yandex");
    }
    
    // Дополнительные специфичные метрики
}
```

### 3. Конфигурация приложения
```yaml
# application.yml
yandex:
  base-url: https://api.partner.market.yandex.ru
  client-id: ${YANDEX_CLIENT_ID}
  api-key: ${YANDEX_API_KEY}
  default-warehouse-id: ${YANDEX_WAREHOUSE_ID}
  max-retry-attempts: 5
  retry-delay-ms: 1000
  retry-multiplier: 2.0
  sync-interval-minutes: 60
  auto-sync-enabled: true
```

### 4. Добавить в docker-compose
```yaml
yandex-service:
  build: ./yandex-service
  ports:
    - "9090:9090"
  environment:
    - SPRING_PROFILES_ACTIVE=docker
  depends_on:
    - postgres
    - kafka
```

## Преимущества стандартизации

1. **Минимальное дублирование кода** - общая логика вынесена в базовые классы
2. **Единообразие API** - все маркетплейсы имеют одинаковые эндпоинты
3. **Простота добавления** - для нового маркетплейса нужно только наследовать базовые классы
4. **Централизованная конфигурация** - общие настройки в базовых классах
5. **Универсальные метрики** - единый подход к мониторингу
6. **Гибкая retry логика** - настраиваемая политика для каждого маркетплейса

## Следующие шаги

1. Создать YandexService по описанной схеме
2. Протестировать работу с Yandex API
3. Адаптировать специфичные части под требования Yandex
4. Добавить в общую систему мониторинга
5. Обновить документацию

