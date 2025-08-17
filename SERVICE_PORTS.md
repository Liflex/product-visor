# Порты сервисов

## HTTP порты

| Сервис | HTTP порт | Описание |
|--------|-----------|----------|
| Authorization Service | 9099 | OAuth2 Authorization Server |
| Order Service | 9088 | REST API для заказов |
| Ozon Service | 9097 | REST API для Ozon |
| Product Visor Backend | 8085 | Основной REST API |

## gRPC порты

| Сервис | gRPC порт | Описание |
|--------|-----------|----------|
| Order Service | 9098 | Внутренний gRPC API |
| Product Visor Backend | 9093 | gRPC API для продуктов |

## Конфигурация gRPC клиентов

### В common-core (GrpcClientsConfig)
```java
@Value("${grpc.client.order-service.address:localhost:9098}")
private String orderServiceAddress;

@Value("${grpc.client.ozon-service.address:localhost:7097}")
private String ozonServiceAddress;
```

### В сервисах
```yaml
# OzonService -> OrderService
grpc:
  client:
    order-service:
      address: localhost:9098

# OrderService -> ProductVisorBackend  
grpc:
  client:
    product-service:
      address: localhost:9093
```

## Проверка подключений

### Проверка HTTP портов
```bash
# Authorization Service
curl http://localhost:9099/actuator/health

# Order Service
curl http://localhost:9088/actuator/health

# Ozon Service
curl http://localhost:9097/actuator/health

# Product Visor Backend
curl http://localhost:8085/actuator/health
```

### Проверка gRPC портов
```bash
# Order Service gRPC
telnet localhost 9098

# Product Visor Backend gRPC
telnet localhost 9093
```

## Частые ошибки

### 1. Неправильный порт OrderService
**Ошибка**: `UNAVAILABLE: Network closed for unknown reason`
**Решение**: Убедиться, что OrderService gRPC работает на порту 9098

### 2. Путаница с портами
- `9099` - Authorization Service (HTTP)
- `9098` - Order Service (gRPC)
- `9097` - Ozon Service (HTTP)
- `9093` - Product Visor Backend (gRPC)

### 3. Сервис не запущен
**Ошибка**: `Connection refused`
**Решение**: Запустить соответствующий сервис

## Отладка

### Логи OrderService
```yaml
logging:
  level:
    net.devh.boot.grpc: DEBUG
    ru.dmitartur.order.grpc: DEBUG
```

### Логи OzonService
```yaml
logging:
  level:
    ru.dmitartur.ozon.service: DEBUG
    io.grpc: DEBUG
```

### Проверка gRPC сервера
```bash
# Проверить, что gRPC сервер запущен
netstat -an | findstr 9098
```
