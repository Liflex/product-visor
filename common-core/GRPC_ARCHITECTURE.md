# Архитектура gRPC в проекте

## Обзор

В проекте используется два типа gRPC конфигураций для разных сценариев взаимодействия:

## 1. Межсервисное взаимодействие (GrpcClientsConfig)

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   OrderService  │    │  OzonService    │    │ ProductService  │
│                 │    │                 │    │                 │
│ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │
│ │GrpcClients  │ │    │ │GrpcClients  │ │    │ │GrpcClients  │ │
│ │Config       │ │    │ │Config       │ │    │ │Config       │ │
│ └─────────────┘ │    │ └─────────────┘ │    │ └─────────────┘ │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Common Core                                  │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │              GrpcClientsConfig                          │    │
│  │                                                         │    │
│  │  • OrderInternalServiceGrpc (с авторизацией)           │    │
│  │  • OzonInternalServiceGrpc (с авторизацией)            │    │
│  │  • MachineTokenService для авторизации                 │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
```

**Особенности:**
- Автоматическая авторизация через `MachineTokenService`
- Использует внутренние API (`OrderInternalService`, `OzonInternalService`)
- Настраивается через `grpc.client.{service}.address`

## 2. Внешние API клиенты (ProductGrpcClientConfig)

```
┌─────────────────┐
│   OrderService  │
│                 │
│ ┌─────────────┐ │
│ │ProductGrpc  │ │◄─── Публичный API без авторизации
│ │ClientConfig │ │
│ └─────────────┘ │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ ProductService  │
│ (localhost:9093)│
│                 │
│ • FindByArticle │
│ • UpdateStock   │
└─────────────────┘
```

**Особенности:**
- Простая конфигурация без авторизации
- Использует публичный API (`ProductService`)
- Настраивается через `grpc.client.product-service.address`

## Proto файлы

### 1. products.proto (публичный API)
```protobuf
service ProductService {
  rpc FindByArticle(FindByArticleRequest) returns (FindByArticleResponse);
  rpc UpdateStockByArticle(UpdateStockRequest) returns (UpdateStockResponse);
}
```

### 2. orders.proto (внутренний API)
```protobuf
service OrderInternalService {
  rpc UpsertOrdersDto (UpsertOrdersRequest) returns (UpsertOrdersResponse);
  rpc FindOrder (FindOrderRequest) returns (FindOrderResponse);
  rpc GetOrdersByMarket (GetOrdersByMarketRequest) returns (GetOrdersByMarketResponse);
}
```

### 3. ozon.proto (внутренний API)
```protobuf
service OzonInternalService {
  rpc GetOrders(JsonRequest) returns (JsonResponse);
  rpc UpdateOrderStatus(JsonRequest) returns (JsonResponse);
  rpc GetProducts(JsonRequest) returns (JsonResponse);
}
```

## Настройки

### Межсервисное взаимодействие
```yaml
grpc:
  client:
    order-service:
      address: localhost:7068
    ozon-service:
      address: localhost:7097
```

### Внешние API
```yaml
grpc:
  client:
    product-service:
      address: localhost:9093
```

## Использование в коде

### Межсервисное взаимодействие
```java
@Service
public class OrderService {
    
    @Autowired
    private OrderInternalServiceGrpc.OrderInternalServiceBlockingStub orderClient;
    
    @Autowired
    private OzonInternalServiceGrpc.OzonInternalServiceBlockingStub ozonClient;
    
    public void someMethod() {
        // Автоматически добавит Bearer токен
        FindOrderResponse response = orderClient.findOrder(
            FindOrderRequest.newBuilder()
                .setPostingNumber("123")
                .build()
        );
    }
}
```

### Внешний API
```java
@Service
public class OrderService {
    
    @Autowired
    private ProductServiceGrpc.ProductServiceBlockingStub productClient;
    
    public void getProductInfo(String article) {
        // Простой вызов без авторизации
        FindByArticleResponse response = productClient.findByArticle(
            FindByArticleRequest.newBuilder()
                .setArticle(article)
                .build()
        );
    }
}
```

## Порты сервисов

| Сервис | gRPC порт | HTTP порт |
|--------|-----------|-----------|
| Authorization Service | 9099 | 9099 |
| Order Service | 9098 | 9088 |
| Product Service | 9093 | 8085 |
| Ozon Service | 7097 | 7097 |

## Заключение

- **GrpcClientsConfig** - для защищенного межсервисного взаимодействия
- **ProductGrpcClientConfig** - для публичных API без авторизации
- Оба типа конфигураций необходимы и не являются дубликатами
