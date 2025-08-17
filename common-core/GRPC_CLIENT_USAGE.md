# gRPC Client Usage Guide

## ProductGrpcClient

`ProductGrpcClient` - это общий gRPC клиент для работы с продуктами, который можно использовать в любом микросервисе.

### Расположение
```
common-core/src/main/java/ru/dmitartur/common/grpc/client/ProductGrpcClient.java
```

### Назначение
Клиент предназначен для внешних вызовов к product-visor-backend сервису через gRPC протокол. Он инкапсулирует логику взаимодействия с ProductService gRPC сервером.

### Использование

#### 1. Внедрение зависимости
```java
@Service
@RequiredArgsConstructor
public class YourService {
    private final ProductGrpcClient productGrpcClient;
}
```

#### 2. Поиск продукта по артикулу
```java
Optional<ProductGrpcClient.ProductInfo> productInfo = 
    productGrpcClient.findProductByArticle("123456789");

if (productInfo.isPresent()) {
    ProductGrpcClient.ProductInfo product = productInfo.get();
    Long productId = product.getId();
    String productName = product.getName();
    // ... использование данных продукта
}
```

#### 3. Обновление остатка товара
```java
boolean success = productGrpcClient.updateProductStockByArticle("123456789", -5);
if (success) {
    // Остаток успешно обновлен
} else {
    // Ошибка при обновлении
}
```

### Структура ProductInfo

```java
public static class ProductInfo {
    private Long id;           // ID продукта
    private String name;       // Название продукта
    private String sku;        // SKU продукта
    private Integer stock;     // Остаток на складе
    private String barcode;    // Штрихкод продукта
}
```

### Конфигурация gRPC клиента

Для использования клиента необходимо настроить gRPC stub в конфигурации микросервиса:

```yaml
# application.yml
grpc:
  client:
    product-service:
      address: localhost:8085  # Адрес product-visor-backend
```

### Обработка ошибок

Клиент автоматически обрабатывает ошибки gRPC вызовов:
- При ошибках поиска возвращает `Optional.empty()`
- При ошибках обновления возвращает `false`
- Все ошибки логируются с соответствующими уровнями

### Логирование

Клиент использует структурированное логирование с эмодзи для лучшей читаемости:
- 🔍 - Поиск продукта
- ✅ - Успешная операция
- ❌ - Ошибка
- ⚠️ - Предупреждение
- 📦 - Операции с остатками

### Примеры использования в микросервисах

#### Order Service
```java
// Поиск продукта для установки productId в заказе
var productInfo = productGrpcClient.findProductByArticle(item.getOfferId());
if (productInfo.isPresent()) {
    item.setProductId(productInfo.get().getId());
}
```

#### Ozon Service
```java
// Обновление остатка после синхронизации заказов
productGrpcClient.updateProductStockByArticle(article, -quantity);
```

### Преимущества использования общего клиента

1. **Переиспользование кода** - один клиент для всех микросервисов
2. **Единообразие** - одинаковый интерфейс во всех сервисах
3. **Централизованная обработка ошибок** - общая логика обработки
4. **Упрощение поддержки** - изменения в одном месте
5. **Консистентное логирование** - единый формат логов
