# Common Core Module

Универсальный модуль для общих компонентов всех сервисов проекта Product Visor.

## Описание

Этот модуль содержит общие компоненты, которые используются во всех сервисах проекта:

- **Security** - компоненты для работы с JWT токенами, авторизацией и аутентификацией
- **DTO** - общие DTO классы (UserDto, OrderDto, etc.)
- **Config** - конфигурации для gRPC, Kafka, Security
- **Utils** - утилиты для работы с JWT
- **gRPC** - protobuf файлы, сгенерированные классы и общие gRPC клиенты

## Основные компоненты

### Security

#### UserDto
Универсальный DTO для пользователя:
```java
@Autowired
private UserDto user;
```

#### CurrentUserDto
DTO для текущего пользователя из JWT токена:
```java
Optional<CurrentUserDto> currentUser = JwtUtil.getCurrentUser();
```

#### MachineTokenService
Сервис для получения технических токенов для межсервисного взаимодействия:
```java
@Autowired
private MachineTokenService tokenService;
String token = tokenService.getBearerToken();
```

#### JwtUtil
Утилиты для работы с JWT токенами:
```java
// Получить ID текущего пользователя
Optional<String> userId = JwtUtil.getCurrentId();

// Получить email текущего пользователя
Optional<String> email = JwtUtil.getCurrentEmail();

// Получить роль текущего пользователя
Optional<Role> role = JwtUtil.getCurrentRole();
```

### Конфигурация

#### OAuth2ClientCredentialsProperties
Конфигурация для OAuth2 client credentials:
```yaml
oauth2:
  client:
    token-uri: http://localhost:9099/oauth2/token
    client-id: your-client-id
    client-secret: your-client-secret
    scope: internal
```

#### CoreProperties
Общие настройки:
```yaml
core:
  # настройки модуля
```

## Использование в других сервисах

### 1. Добавить зависимость в pom.xml
```xml
<dependency>
    <groupId>ru.dmitartur</groupId>
    <artifactId>common-core</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 2. Автоконфигурация
Модуль автоматически настраивается через `@EnableAutoConfiguration`. 
Все компоненты будут доступны в Spring Context.

### 3. Использование компонентов
```java
@Service
public class MyService {
    
    @Autowired
    private MachineTokenService tokenService;
    
    public void someMethod() {
        // Получить технический токен
        String token = tokenService.getBearerToken();
        
        // Получить текущего пользователя
        Optional<CurrentUserDto> user = JwtUtil.getCurrentUser();
    }
}
```

## Межсервисное взаимодействие

### RestTemplate с автоматической авторизацией
```java
@Autowired
private RestTemplate restTemplate;

// Автоматически добавит Bearer токен для /internal/ эндпоинтов
restTemplate.getForObject("http://other-service/internal/api/data", String.class);
```

### WebClient с автоматической авторизацией
```java
@Autowired
private WebClient webClient;

// Автоматически добавит Bearer токен для /internal/ эндпоинтов
webClient.get()
    .uri("http://other-service/internal/api/data")
    .retrieve()
    .bodyToMono(String.class);
```

### Feign с автоматической авторизацией
```java
@FeignClient(name = "other-service")
public interface OtherServiceClient {
    
    // Автоматически добавит Bearer токен для /internal/ эндпоинтов
    @GetMapping("/internal/api/data")
    String getData();
}
```

### gRPC клиенты
Общие gRPC клиенты для межсервисного взаимодействия:

#### ProductGrpcClient
Клиент для работы с продуктами через gRPC:
```java
@Autowired
private ProductGrpcClient productGrpcClient;

// Поиск продукта по артикулу
Optional<ProductGrpcClient.ProductInfo> product = 
    productGrpcClient.findProductByArticle("123456789");

// Обновление остатка
boolean success = productGrpcClient.updateProductStockByArticle("123456789", -5);
```

Подробная документация: [GRPC_CLIENT_USAGE.md](GRPC_CLIENT_USAGE.md)

## Роли пользователей

```java
public enum Role {
    PRIVATE_PERSON("частное лицо"),
    EMPLOYEE("сотрудник"),
    ADMIN("администратор заведения");
}
```

## JWT Claims

Стандартные claims в JWT токенах:
- `user_id` - ID пользователя
- `email` - Email пользователя
- `role` - Роль пользователя
- `locale` - Локаль пользователя
- `timezone` - Часовой пояс пользователя

## Сборка

```bash
mvn clean compile
```

## Тестирование

```bash
mvn test
```
