# Рефакторинг ProductGrpcService

## Что было сделано

### 1. Вынос в common-core
- Создан общий `ProductGrpcClient` в пакете `ru.dmitartur.common.grpc.client`
- Удален дублирующий `ProductGrpcService` из order-service
- Обновлен `ProductService` в order-service для использования общего клиента

### 2. Структура изменений

#### До рефакторинга:
```
order-service/
  └── src/main/java/ru/dmitartur/order/service/product/
      ├── ProductService.java (использовал ProductGrpcService)
      └── ProductGrpcService.java (локальный gRPC клиент)
```

#### После рефакторинга:
```
common-core/
  └── src/main/java/ru/dmitartur/common/grpc/client/
      └── ProductGrpcClient.java (общий gRPC клиент)

order-service/
  └── src/main/java/ru/dmitartur/order/service/product/
      └── ProductService.java (использует общий ProductGrpcClient)
```

### 3. Преимущества

1. **Переиспользование кода** - один клиент для всех микросервисов
2. **Единообразие** - одинаковый интерфейс во всех сервисах  
3. **Централизованная обработка ошибок** - общая логика обработки
4. **Упрощение поддержки** - изменения в одном месте
5. **Консистентное логирование** - единый формат логов

### 4. Использование

#### В любом микросервисе:
```java
@Service
@RequiredArgsConstructor
public class YourService {
    private final ProductGrpcClient productGrpcClient;
    
    public void someMethod() {
        Optional<ProductGrpcClient.ProductInfo> product = 
            productGrpcClient.findProductByArticle("123456789");
    }
}
```

### 5. Документация
- Создан `GRPC_CLIENT_USAGE.md` с подробной документацией
- Обновлен `README.md` в common-core
- Добавлены примеры использования

### 6. Название
Выбрано название `ProductGrpcClient` как наиболее подходящее для:
- **Product** - указывает на тематику (работа с продуктами)
- **Grpc** - указывает на протокол взаимодействия
- **Client** - указывает на назначение (клиент для внешних вызовов)

### 7. Следующие шаги
- Протестировать работу в order-service
- Рассмотреть возможность использования в ozon-service
- Добавить дополнительные методы при необходимости

