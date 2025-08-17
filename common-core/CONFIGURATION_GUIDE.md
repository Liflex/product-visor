# Руководство по конфигурациям Common Core

## Обзор

Common Core модуль предоставляет общие конфигурации для всех сервисов проекта. Это позволяет избежать дублирования кода и обеспечить единообразие настроек.

## Доступные конфигурации

### 1. KafkaCommonConfig
**Файл:** `ru.dmitartur.common.config.KafkaCommonConfig`

**Что предоставляет:**
- `ObjectMapper` - для сериализации/десериализации JSON (если не настроен)
- `stringProducerFactory` - дополнительная фабрика для Kafka producer со строковыми сериализаторами
- `stringKafkaTemplate` - дополнительный шаблон для отправки строковых сообщений в Kafka

**Использование:**
```java
// Основной KafkaTemplate (создается Spring Boot автоматически)
@Autowired
private KafkaTemplate<String, String> kafkaTemplate;

// Дополнительный KafkaTemplate для строковых сообщений
@Autowired
@Qualifier("stringKafkaTemplate")
private KafkaTemplate<String, String> stringKafkaTemplate;

// Отправка сообщения
kafkaTemplate.send("topic-name", "message");
```

### 2. CorsCommonConfig
**Файл:** `ru.dmitartur.common.config.CorsCommonConfig`

**Что предоставляет:**
- CORS конфигурация для всех эндпоинтов (`/**`)
- Поддержка всех HTTP методов (GET, POST, PUT, DELETE, OPTIONS)
- Разрешение всех заголовков
- Поддержка credentials

**Автоматически применяется** ко всем сервисам, использующим common-core.

### 3. SecurityConfig
**Файл:** `ru.dmitartur.common.config.SecurityConfig`

**Что предоставляет:**
- Универсальная конфигурация безопасности для OAuth2 Resource Server
- 4 цепочки безопасности:
  - Actuator (открыт для health/info/prometheus)
  - Rqueue (открыт для дашборда)
  - Internal (требует scope 'internal')
  - Default (требует аутентификации)

**Автоматически применяется** ко всем сервисам, использующим common-core.

### 4. GrpcClientsConfig
**Файл:** `ru.dmitartur.common.config.GrpcClientsConfig`

**Что предоставляет:**
- gRPC клиенты для **межсервисного** взаимодействия
- Автоматическая авторизация через MachineTokenService
- Клиенты для OrderService и OzonService (внутренние API)

**Назначение:** Когда один сервис должен вызвать **другой сервис** с авторизацией

**Использование:**
```java
@Autowired
private OrderInternalServiceGrpc.OrderInternalServiceBlockingStub orderClient;

@Autowired
private OzonInternalServiceGrpc.OzonInternalServiceBlockingStub ozonClient;
```

**Настройки:**
```yaml
grpc:
  client:
    order-service:
      address: localhost:7068
    ozon-service:
      address: localhost:7097
```

### 5. WebClientMachineTokenFilter
**Файл:** `ru.dmitartur.common.security.WebClientMachineTokenFilter`

**Что предоставляет:**
- WebClient с автоматической авторизацией для /internal/ эндпоинтов
- Автоматическое добавление Bearer токена

**Использование:**
```java
@Autowired
private WebClient webClient;

// Автоматически добавит Bearer токен для /internal/ эндпоинтов
webClient.get()
    .uri("http://other-service/internal/api/data")
    .retrieve()
    .bodyToMono(String.class);
```

## Удаленные дубликаты

Следующие конфигурации были удалены из отдельных сервисов и заменены на общие:

### OrderService
- ❌ `KafkaConfig.java` → ✅ `KafkaCommonConfig`
- ❌ `CorsConfig.java` → ✅ `CorsCommonConfig`

### OzonService
- ❌ `CorsConfig.java` → ✅ `CorsCommonConfig`

### ProductVisorBackend
- ❌ `KafkaConfig.java` → ✅ `KafkaCommonConfig`
- ❌ `CorsConfig.java` → ✅ `CorsCommonConfig`

## gRPC Конфигурации - Различия

### GrpcClientsConfig (common-core)
- **Назначение**: Межсервисное взаимодействие с авторизацией
- **Использует**: `OrderInternalServiceGrpc`, `OzonInternalServiceGrpc`
- **Особенности**: Автоматическая авторизация через MachineTokenService
- **Применение**: Когда сервис вызывает другой сервис

### ProductGrpcClientConfig (OrderService)
- **Назначение**: Внешний API без авторизации
- **Использует**: `ProductServiceGrpc` (публичный API)
- **Особенности**: Простая конфигурация без авторизации
- **Применение**: Когда OrderService получает данные о продуктах

**Вывод**: Это НЕ дубликаты, а разные типы конфигураций для разных целей!

## Добавление в новый сервис

### 1. Добавить зависимость
```xml
<dependency>
    <groupId>ru.dmitartur</groupId>
    <artifactId>common-core</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 2. Импортировать общие настройки
```yaml
# application.yml
spring:
  config:
    import:
      - classpath:application-common.yml
  application:
    name: my-service
  datasource:
    url: jdbc:postgresql://localhost:5433/product_visor
    username: postgres
    password: postgres
  jpa:
    default-schema: my_service

server:
  port: 9090
```

### 3. Автоконфигурация
Все конфигурации автоматически подключаются через `@EnableAutoConfiguration`.

### 3. Использование
```java
@Service
public class MyService {
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private WebClient webClient;
    
    @Autowired
    private MachineTokenService tokenService;
    
    public void someMethod() {
        // Отправить сообщение в Kafka
        kafkaTemplate.send("my-topic", "message");
        
        // Вызвать другой сервис с авторизацией
        webClient.get()
            .uri("http://other-service/internal/api/data")
            .retrieve()
            .bodyToMono(String.class);
    }
}
```

## Специфичные конфигурации

Если сервису нужна специфичная конфигурация, которая отличается от общей:

1. **Создайте отдельный конфиг** в сервисе
2. **Используйте `@Primary`** для переопределения бина из common-core
3. **Или используйте `@ConditionalOnProperty`** для условной конфигурации

Пример:
```java
@Configuration
@ConditionalOnProperty(name = "custom.kafka.enabled", havingValue = "true")
public class CustomKafkaConfig {
    
    @Bean
    @Primary
    public KafkaTemplate<String, String> customKafkaTemplate() {
        // Специфичная конфигурация
    }
}
```

## Настройки

### Kafka
```yaml
# Основные настройки Kafka (Spring Boot)
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all
      retries: 3

# Дополнительные настройки для сервисов
kafka:
  enabled: true
  topics:
    order-events: order-events
  consumer:
    group-id: product-visor-group
```

### gRPC
```yaml
grpc:
  client:
    order-service:
      address: localhost:7068
    ozon-service:
      address: localhost:7097
```

### OAuth2
```yaml
oauth2:
  client:
    token-uri: http://localhost:9099/oauth2/token
    client-id: oficiant-client
    client-secret: oficiant-secret-90489bc550923ed2
    scope: internal
```

### Security
```yaml
security:
  oauth2:
    resourceserver:
      jwt:
        jwk-set-uri: http://localhost:9099/oauth2/jwks
```
