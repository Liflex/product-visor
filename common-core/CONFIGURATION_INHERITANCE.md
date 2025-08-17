# Наследование конфигураций из Common Core

## Обзор

Все сервисы теперь используют общие настройки из `application-common.yml` через механизм `spring.config.import`.

## Как это работает

### 1. Общие настройки в common-core
**Файл:** `common-core/src/main/resources/application-common.yml`

Содержит настройки, которые используются всеми сервисами:
- Spring DataSource (драйвер)
- JPA (диалект, форматирование SQL)
- Flyway (базовые настройки)
- OAuth2 (токены, клиенты)
- Security (JWT, ресурсный сервер)
- Kafka (bootstrap-servers, сериализаторы)
- Management (endpoints)
- Logging (уровни для Spring компонентов)

### 2. Импорт в сервисах
**Файл:** `{service}/src/main/resources/application.yml`

```yaml
spring:
  config:
    import:
      - classpath:application-common.yml
  application:
    name: order-service
  # Специфичные настройки сервиса
  datasource:
    url: jdbc:postgresql://localhost:5433/product_visor
    username: postgres
    password: postgres
```

## Приоритет настроек

Spring Boot загружает конфигурации в следующем порядке (от низшего к высшему приоритету):

1. **application-common.yml** (из common-core)
2. **application.yml** (в сервисе)
3. **application-{profile}.yml** (профили)
4. **Переменные окружения**
5. **Системные свойства**

**Вывод**: Настройки в сервисе **переопределяют** общие настройки.

## Примеры использования

### OrderService
```yaml
spring:
  config:
    import:
      - classpath:application-common.yml
  application:
    name: order-service
  datasource:
    url: jdbc:postgresql://localhost:5433/product_visor
    username: postgres
    password: postgres
  jpa:
    default-schema: orders  # Переопределяет общую настройку
  flyway:
    schemas: orders         # Переопределяет общую настройку

# Специфичные настройки
grpc:
  server:
    port: 9098
  client:
    product-service:
      address: localhost:9093

server:
  port: 9088
```

### ProductVisorBackend
```yaml
spring:
  config:
    import:
      - classpath:application-common.yml
  servlet:
    multipart:
      max-file-size: 10MB
  datasource:
    url: jdbc:postgresql://localhost:5433/product_visor
    username: postgres
    password: postgres
  jpa:
    default_schema: visor  # Переопределяет общую настройку

# Специфичные настройки
server:
  port: 8085

grpc:
  server:
    port: 9093

file:
  upload-dir: F:\images
```

### OzonService
```yaml
spring:
  config:
    import:
      - classpath:application-common.yml
  application:
    name: ozon-service
  datasource:
    url: jdbc:postgresql://localhost:5433/product_visor
    username: postgres
    password: postgres
  jpa:
    default_schema: ozon  # Переопределяет общую настройку
  flyway:
    schemas: ozon         # Переопределяет общую настройку

# Специфичные настройки
ozon:
  base-url: ${OZON_BASE_URL:https://api-seller.ozon.ru}
  client-id: ${OZON_CLIENT_ID:2960590}
  api-key: ${OZON_API_KEY:026e58e9-1546-4dd2-b47c-b9f111c1f587}

server:
  port: 9097
```

## Что наследуется автоматически

### 1. Spring DataSource
```yaml
# Из application-common.yml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver

# В сервисе нужно указать только специфичное
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/product_visor
    username: postgres
    password: postgres
```

### 2. JPA
```yaml
# Из application-common.yml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

# В сервисе можно переопределить
spring:
  jpa:
    default-schema: orders  # Специфичная схема
    show-sql: false         # Переопределение
```

### 3. Kafka
```yaml
# Из application-common.yml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer

kafka:
  enabled: true
  topics:
    order-events: order-events
  consumer:
    group-id: product-visor-group
```

### 4. Security
```yaml
# Из application-common.yml
security:
  oauth2:
    resourceserver:
      jwt:
        jwk-set-uri: ${JWK_SET_URI:http://localhost:9099/oauth2/jwks}

oauth2:
  client:
    token-uri: http://localhost:9099/oauth2/token
    client-id: oficiant-client
    client-secret: oficiant-secret-90489bc550923ed2
    scope: internal
```

## Добавление нового сервиса

### 1. Создайте application.yml
```yaml
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

### 2. Добавьте зависимость на common-core
```xml
<dependency>
    <groupId>ru.dmitartur</groupId>
    <artifactId>common-core</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 3. Готово!
Сервис автоматически получит:
- ✅ Настройки Kafka
- ✅ Настройки Security
- ✅ Настройки OAuth2
- ✅ Общие конфигурации (CORS, gRPC клиенты)
- ✅ Настройки логирования

## Отладка

### Проверка загрузки конфигураций
```bash
# В логах Spring Boot ищите:
# Loading config from 'classpath:application-common.yml'
```

### Проверка приоритетов
```yaml
# В application-common.yml
logging:
  level:
    root: INFO

# В сервисе
logging:
  level:
    ru.dmitartur.my: DEBUG  # Переопределяет общую настройку
```

### Проверка переменных окружения
```bash
# Переменные окружения имеют высший приоритет
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/my_db
export SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

## Преимущества

1. **DRY принцип** - нет дублирования настроек
2. **Централизованное управление** - изменения в одном месте
3. **Консистентность** - все сервисы используют одинаковые настройки
4. **Гибкость** - каждый сервис может переопределить нужные настройки
5. **Простота** - минимум конфигурации в каждом сервисе
