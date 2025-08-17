# Проверка работы конфигураций

## Как проверить, что application-common.yml загружается

### 1. Проверка в логах Spring Boot

При запуске сервиса в логах должно появиться:
```
Loading config from 'classpath:application-common.yml'
```

### 2. Проверка через Environment

```java
@Autowired
private Environment environment;

public void checkConfig() {
    // Проверяем настройки Kafka
    String kafkaBootstrapServers = environment.getProperty("spring.kafka.bootstrap-servers");
    System.out.println("Kafka bootstrap servers: " + kafkaBootstrapServers);
    
    // Проверяем настройки OAuth2
    String oauth2TokenUri = environment.getProperty("oauth2.client.token-uri");
    System.out.println("OAuth2 token URI: " + oauth2TokenUri);
    
    // Проверяем настройки Security
    String jwkSetUri = environment.getProperty("security.oauth2.resourceserver.jwt.jwk-set-uri");
    System.out.println("JWT JWK set URI: " + jwkSetUri);
}
```

### 3. Проверка бинов

```java
@Autowired
private ApplicationContext context;

public void checkBeans() {
    // Проверяем, что бины из common-core созданы
    boolean hasKafkaTemplate = context.containsBean("kafkaTemplate");
    boolean hasStringKafkaTemplate = context.containsBean("stringKafkaTemplate");
    boolean hasObjectMapper = context.containsBean("objectMapper");
    
    System.out.println("KafkaTemplate exists: " + hasKafkaTemplate);
    System.out.println("StringKafkaTemplate exists: " + hasStringKafkaTemplate);
    System.out.println("ObjectMapper exists: " + hasObjectMapper);
}
```

### 4. Проверка через Actuator

Если включен Spring Boot Actuator, можно проверить конфигурацию через endpoint:

```bash
# Получить все свойства
curl http://localhost:9088/actuator/env

# Получить конкретное свойство
curl http://localhost:9088/actuator/env/spring.kafka.bootstrap-servers
```

### 5. Проверка приоритетов

```java
@Value("${spring.kafka.bootstrap-servers}")
private String kafkaBootstrapServers;

@Value("${spring.application.name}")
private String applicationName;

@PostConstruct
public void checkPriorities() {
    // Должно быть из application-common.yml
    System.out.println("Kafka bootstrap servers: " + kafkaBootstrapServers);
    
    // Должно быть из application.yml сервиса
    System.out.println("Application name: " + applicationName);
}
```

## Ожидаемые значения

### Из application-common.yml
- `spring.kafka.bootstrap-servers` = `localhost:9092`
- `oauth2.client.token-uri` = `http://localhost:9099/oauth2/token`
- `oauth2.client.client-id` = `oficiant-client`
- `security.oauth2.resourceserver.jwt.jwk-set-uri` = `http://localhost:9099/oauth2/jwks`

### Из application.yml сервиса
- `spring.application.name` = `order-service` (или другое имя сервиса)
- `spring.datasource.url` = `jdbc:postgresql://localhost:5433/product_visor`
- `spring.jpa.default-schema` = `orders` (или другая схема)

## Устранение проблем

### Проблема: Файл не найден
```
Could not resolve placeholder 'spring.kafka.bootstrap-servers'
```

**Решение:**
1. Проверьте, что `common-core` собран: `mvn clean install`
2. Проверьте, что зависимость добавлена в `pom.xml`
3. Проверьте, что `spring.config.import` настроен правильно

### Проблема: Конфликт бинов
```
The bean 'kafkaTemplate' could not be registered
```

**Решение:**
1. Убедитесь, что используется `@ConditionalOnMissingBean` в `KafkaCommonConfig`
2. Проверьте, что нет дублирующих конфигураций в сервисе

### Проблема: Настройки не переопределяются
```
Общие настройки не переопределяются специфичными
```

**Решение:**
1. Проверьте порядок в `spring.config.import`
2. Убедитесь, что специфичные настройки находятся в правильном месте в `application.yml`

## Пример проверки для OrderService

```java
@Component
public class ConfigurationChecker {
    
    @Autowired
    private Environment environment;
    
    @PostConstruct
    public void checkConfiguration() {
        log.info("=== Configuration Check ===");
        
        // Проверяем общие настройки
        log.info("Kafka bootstrap servers: {}", 
            environment.getProperty("spring.kafka.bootstrap-servers"));
        log.info("OAuth2 token URI: {}", 
            environment.getProperty("oauth2.client.token-uri"));
        
        // Проверяем специфичные настройки
        log.info("Application name: {}", 
            environment.getProperty("spring.application.name"));
        log.info("Database schema: {}", 
            environment.getProperty("spring.jpa.default-schema"));
        
        log.info("=== Configuration Check Complete ===");
    }
}
```
