# Примеры использования gRPC сервисов

## 1. Межсервисное взаимодействие (с авторизацией)

### OrderInternalService
```java
@Service
public class OrderService {
    
    @Autowired
    private OrderInternalServiceGrpc.OrderInternalServiceBlockingStub orderClient;
    
    public void upsertOrders(List<OrderDto> orders) {
        UpsertOrdersRequest request = UpsertOrdersRequest.newBuilder()
            .addAllOrders(orders.stream()
                .map(this::mapToGrpcOrder)
                .collect(Collectors.toList()))
            .build();
            
        UpsertOrdersResponse response = orderClient.upsertOrdersDto(request);
        
        if (!response.getSuccess()) {
            log.error("Failed to upsert orders: {}", response.getErrorsList());
        }
    }
    
    public OrderDto findOrder(String postingNumber) {
        FindOrderRequest request = FindOrderRequest.newBuilder()
            .setPostingNumber(postingNumber)
            .build();
            
        FindOrderResponse response = orderClient.findOrder(request);
        
        if (response.getFound()) {
            return mapFromGrpcOrder(response.getOrder());
        }
        return null;
    }
    
    public List<OrderDto> getOrdersByMarket(Market market, int page, int size) {
        GetOrdersByMarketRequest request = GetOrdersByMarketRequest.newBuilder()
            .setMarket(market)
            .setPage(page)
            .setSize(size)
            .build();
            
        GetOrdersByMarketResponse response = orderClient.getOrdersByMarket(request);
        
        return response.getOrdersList().stream()
            .map(this::mapFromGrpcOrder)
            .collect(Collectors.toList());
    }
}
```

### OzonInternalService
```java
@Service
public class OzonService {
    
    @Autowired
    private OzonInternalServiceGrpc.OzonInternalServiceBlockingStub ozonClient;
    
    public String getOrdersFromOzon(String requestJson) {
        JsonRequest request = JsonRequest.newBuilder()
            .setJson(requestJson)
            .build();
            
        JsonResponse response = ozonClient.getOrders(request);
        return response.getJson();
    }
    
    public String updateOrderStatusInOzon(String requestJson) {
        JsonRequest request = JsonRequest.newBuilder()
            .setJson(requestJson)
            .build();
            
        JsonResponse response = ozonClient.updateOrderStatus(request);
        return response.getJson();
    }
    
    public String getProductsFromOzon(String requestJson) {
        JsonRequest request = JsonRequest.newBuilder()
            .setJson(requestJson)
            .build();
            
        JsonResponse response = ozonClient.getProducts(request);
        return response.getJson();
    }
}
```

## 2. Внешние API (без авторизации)

### ProductService
```java
@Service
public class ProductService {
    
    @Autowired
    private ProductServiceGrpc.ProductServiceBlockingStub productClient;
    
    public ProductInfoDto findProductByArticle(String article) {
        FindByArticleRequest request = FindByArticleRequest.newBuilder()
            .setArticle(article)
            .build();
            
        FindByArticleResponse response = productClient.findByArticle(request);
        
        if (response.getFound()) {
            return response.getProduct();
        }
        return null;
    }
    
    public boolean updateStockByArticle(String article, int quantityChange) {
        UpdateStockRequest request = UpdateStockRequest.newBuilder()
            .setArticle(article)
            .setQuantityChange(quantityChange)
            .build();
            
        UpdateStockResponse response = productClient.updateStockByArticle(request);
        return response.getSuccess();
    }
}
```

## 3. Настройки в application.yml

### Для сервиса, который использует межсервисное взаимодействие
```yaml
grpc:
  client:
    order-service:
      address: localhost:7068
    ozon-service:
      address: localhost:7097
```

### Для сервиса, который использует внешние API
```yaml
grpc:
  client:
    product-service:
      address: localhost:9093
```

## 4. Обработка ошибок

```java
@Service
public class GrpcErrorHandler {
    
    public <T> T handleGrpcCall(Supplier<T> grpcCall, String operation) {
        try {
            return grpcCall.get();
        } catch (StatusRuntimeException e) {
            log.error("gRPC {} failed with status: {}", operation, e.getStatus());
            throw new ServiceException("Failed to " + operation, e);
        } catch (Exception e) {
            log.error("Unexpected error during gRPC {}", operation, e);
            throw new ServiceException("Unexpected error during " + operation, e);
        }
    }
    
    // Использование
    public OrderDto findOrder(String postingNumber) {
        return handleGrpcCall(
            () -> orderClient.findOrder(FindOrderRequest.newBuilder()
                .setPostingNumber(postingNumber)
                .build()),
            "find order"
        );
    }
}
```

## 5. Асинхронные вызовы

```java
@Service
public class AsyncGrpcService {
    
    @Autowired
    private OrderInternalServiceGrpc.OrderInternalServiceStub orderClient;
    
    public CompletableFuture<OrderDto> findOrderAsync(String postingNumber) {
        CompletableFuture<OrderDto> future = new CompletableFuture<>();
        
        FindOrderRequest request = FindOrderRequest.newBuilder()
            .setPostingNumber(postingNumber)
            .build();
            
        orderClient.findOrder(request, new StreamObserver<FindOrderResponse>() {
            @Override
            public void onNext(FindOrderResponse response) {
                if (response.getFound()) {
                    future.complete(mapFromGrpcOrder(response.getOrder()));
                } else {
                    future.complete(null);
                }
            }
            
            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }
            
            @Override
            public void onCompleted() {
                // Уже обработано в onNext
            }
        });
        
        return future;
    }
}
```

## 6. Метрики и мониторинг

```java
@Component
public class GrpcMetrics {
    
    private final MeterRegistry meterRegistry;
    
    public GrpcMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    public <T> T timeGrpcCall(String service, String method, Supplier<T> call) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            T result = call.get();
            sample.stop(Timer.builder("grpc.client.duration")
                .tag("service", service)
                .tag("method", method)
                .tag("status", "success")
                .register(meterRegistry));
            return result;
        } catch (Exception e) {
            sample.stop(Timer.builder("grpc.client.duration")
                .tag("service", service)
                .tag("method", method)
                .tag("status", "error")
                .register(meterRegistry));
            throw e;
        }
    }
}
```
