# Library Marketplace

Библиотека для работы с маркетплейсами. Содержит базовые классы и интерфейсы для стандартизации работы с различными маркетплейсами.

## Структура модуля

```
library-marketplace/
├── src/main/java/ru/dmitartur/library/marketplace/
│   ├── controller/
│   │   └── BaseMarketplaceController.java          # Базовый контроллер
│   ├── service/
│   │   └── BaseMarketplaceService.java             # Базовый интерфейс сервиса
│   ├── integration/
│   │   └── BaseMarketplaceApi.java                 # Базовый интерфейс API
│   ├── scheduled/
│   │   └── BaseMarketplaceScheduler.java           # Базовый интерфейс планировщика
│   ├── retry/
│   │   ├── BaseMarketplaceRetryPolicy.java         # Базовая политика retry
│   │   ├── BaseMarketplaceApiException.java        # Базовое исключение API
│   │   └── BaseMarketplaceRetryService.java        # Базовый сервис retry
│   ├── config/
│   │   └── BaseMarketplaceProperties.java          # Базовые свойства
│   ├── mapper/
│   │   └── BaseMarketplaceOrderMapper.java         # Базовый интерфейс маппера
│   ├── metrics/
│   │   └── BaseMarketplaceMetrics.java             # Базовый класс метрик
│   ├── entity/
│   │   ├── BaseMarketplaceEntity.java              # Базовая Entity
│   │   └── BaseMarketplaceCredentials.java         # Базовая Entity для учетных данных
│   └── dto/
│       ├── DateRangeDto.java                       # DTO для диапазона дат
│       └── SyncStatusResponse.java                 # DTO для статуса синхронизации
```

## Зависимости

- `common-core` - базовые retry классы
- `spring-boot-starter-web` - веб-функциональность
- `spring-boot-starter-data-jpa` - JPA поддержка
- `spring-retry` - retry функциональность
- `micrometer-core` - метрики
- `jackson-databind` - JSON обработка

## Использование

### 1. Добавить зависимость в pom.xml

```xml
<dependency>
    <groupId>ru.dmitartur</groupId>
    <artifactId>library-marketplace</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. Наследовать базовые классы

#### Контроллер
```java
@RestController
@RequestMapping("/api/ozon")
public class OzonController extends BaseMarketplaceController {
    
    public OzonController() {
        super("Ozon", "/api/ozon");
    }
    
    @Override
    protected BaseMarketplaceService getMarketplaceService() {
        return ozonService;
    }
    
    @Override
    protected BaseMarketplaceApi getMarketplaceApi() {
        return ozonApi;
    }
    
    @Override
    protected BaseMarketplaceScheduler getScheduler() {
        return ozonScheduler;
    }
}
```

#### Сервис
```java
@Service
public class OzonService implements BaseMarketplaceService {
    
    @Override
    public String getMarketplaceName() {
        return "Ozon";
    }
    
    // Реализовать все методы интерфейса
}
```

#### API
```java
@Component
public class OzonApi implements BaseMarketplaceApi {
    
    @Override
    public String getMarketplaceName() {
        return "Ozon";
    }
    
    // Реализовать все методы интерфейса
}
```

#### Планировщик
```java
@Component
public class OzonScheduler implements BaseMarketplaceScheduler {
    
    @Override
    public String getMarketplaceName() {
        return "Ozon";
    }
    
    // Реализовать все методы интерфейса
}
```

#### Retry политика
```java
@Component
public class OzonRetryPolicy extends BaseMarketplaceRetryPolicy {
    
    public OzonRetryPolicy() {
        super("Ozon", 5);
    }
    
    @Override
    protected boolean isRetryableError(String errorCode, String errorMessage) {
        // Специфичная логика для Ozon
        switch (errorCode) {
            case "TOO_MANY_REQUESTS":
            case "RATE_LIMIT_EXCEEDED":
                return true;
            default:
                return false;
        }
    }
}
```

#### Исключения
```java
public class OzonApiException extends BaseMarketplaceApiException {
    
    public OzonApiException(String errorCode, String errorMessage) {
        super("Ozon", errorCode, errorMessage);
    }
    
    @Override
    public boolean isRetryable() {
        return "TOO_MANY_REQUESTS".equals(getErrorCode());
    }
}
```

#### Конфигурация
```java
@Component
@ConfigurationProperties(prefix = "ozon")
public class OzonProperties extends BaseMarketplaceProperties {
    
    @Override
    public String getMarketplaceName() {
        return "Ozon";
    }
}
```

#### Маппер
```java
@Component
public class OzonOrderMapper implements BaseMarketplaceOrderMapper {
    
    @Override
    public String getMarketplaceName() {
        return "Ozon";
    }
    
    @Override
    public String getOrderSource() {
        return "OZON_FBO";
    }
    
    // Реализовать все методы интерфейса
}
```

#### Метрики
```java
@Component
public class OzonMetrics extends BaseMarketplaceMetrics {
    
    public OzonMetrics(MeterRegistry meterRegistry) {
        super(meterRegistry, "Ozon");
    }
}
```

#### Entity
```java
@Entity
@Table(name = "ozon_credentials")
public class OzonCredentials extends BaseMarketplaceCredentials {
    
    @Override
    public String getMarketplaceName() {
        return "Ozon";
    }
}
```

## Преимущества

1. **Стандартизация** - единый интерфейс для всех маркетплейсов
2. **Переиспользование** - общая логика вынесена в базовые классы
3. **Расширяемость** - легко добавлять новые маркетплейсы
4. **Метрики** - встроенная система мониторинга
5. **Retry логика** - надежная обработка ошибок
6. **Конфигурация** - централизованные настройки

## Следующие шаги

1. Перевести существующие Ozon классы на наследование от базовых
2. Создать YandexService используя эту библиотеку
3. Добавить специфичные для маркетплейсов расширения
4. Создать тесты для базовых классов

