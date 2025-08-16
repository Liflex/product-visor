# Централизованная конфигурация портов

## Обзор

Все порты и URL сервисов централизованы в `common-core` для обеспечения консистентности между сервисами.

## Файлы конфигурации

- `ServicePorts.java` - константы портов
- `ServiceUrls.java` - готовые URL для подключения
- `ServiceConfig.java` - утилиты для получения конфигурации

## Текущие порты

### HTTP порты
- **product-visor-backend**: 8085
- **order-service**: 9088  
- **ozon-service**: 9097

### gRPC порты
- **product-visor-backend**: 9093 (изменен с 9092)
- **order-service**: 9091

### Инфраструктура
- **PostgreSQL**: 5433
- **Kafka**: 9092
- **Prometheus**: 9090
- **Grafana**: 3000

## Использование в сервисах

### В application.yml
```yaml
server:
  port: 8085  # Используйте константу из ServicePorts

grpc:
  server:
    port: 9093  # Используйте константу из ServicePorts
  client:
    product-service:
      address: localhost:9093  # Используйте URL из ServiceUrls
```

### В коде Java
```java
import ru.dmitartur.common.config.ServicePorts;
import ru.dmitartur.common.config.ServiceUrls;
import ru.dmitartur.common.config.ServiceConfig;

// Получить порт
int port = ServicePorts.PRODUCT_VISOR_BACKEND_HTTP;

// Получить URL
String url = ServiceUrls.PRODUCT_VISOR_BACKEND_HTTP_URL;

// Или через утилиту
String grpcUrl = ServiceConfig.getProductServiceGrpcUrl();
```

## Изменение портов

1. Измените константу в `ServicePorts.java`
2. Обновите все сервисы, использующие этот порт
3. Перезапустите сервисы

## Преимущества

- ✅ Единое место для управления портами
- ✅ Автоматическая синхронизация между сервисами
- ✅ Легкое изменение портов
- ✅ Предотвращение конфликтов портов
- ✅ Документированная архитектура
