# Журнал изменений Common Core

## [1.0.0] - 2024-01-XX

### ✅ Добавлено
- **KafkaCommonConfig** - общая конфигурация Kafka с `@ConditionalOnMissingBean`
- **CorsCommonConfig** - общая CORS конфигурация для всех сервисов
- **SecurityConfig** - универсальная конфигурация безопасности OAuth2
- **GrpcClientsConfig** - gRPC клиенты для межсервисного взаимодействия
- **WebClientMachineTokenFilter** - WebClient с автоматической авторизацией
- **UserDto** - универсальный DTO для пользователей
- **application-common.yml** - общие настройки для всех сервисов

### 🔧 Изменено
- **KafkaCommonConfig** - исправлен конфликт бинов с Spring Boot
- **GrpcClientsConfig** - добавлен `OzonInternalServiceGrpc`
- **application-common.yml** - настройки Kafka перемещены под `spring.kafka`

### ❌ Удалено
- **KafkaConfig.java** из OrderService, ProductVisorBackend
- **CorsConfig.java** из OrderService, OzonService, ProductVisorBackend
- Дублирующие настройки Kafka из сервисов

### 📚 Документация
- **CONFIGURATION_GUIDE.md** - руководство по конфигурациям
- **GRPC_ARCHITECTURE.md** - архитектура gRPC
- **GRPC_USAGE_EXAMPLES.md** - примеры использования gRPC
- **KAFKA_TROUBLESHOOTING.md** - устранение проблем с Kafka
- **CONFIGURATION_INHERITANCE.md** - наследование конфигураций

## Настройка сервисов

### До изменений
```yaml
# Каждый сервис дублировал настройки
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
# ... и так далее в каждом сервисе
```

### После изменений
```yaml
# В common-core/src/main/resources/application-common.yml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer

# В сервисе
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
    default-schema: orders  # Только специфичные настройки
```

## Преимущества

1. **DRY принцип** - нет дублирования настроек
2. **Централизованное управление** - изменения в одном месте
3. **Консистентность** - все сервисы используют одинаковые настройки
4. **Гибкость** - каждый сервис может переопределить нужные настройки
5. **Простота** - минимум конфигурации в каждом сервисе

## Миграция

### Для существующих сервисов
1. Добавить `spring.config.import` в `application.yml`
2. Удалить дублирующие настройки
3. Оставить только специфичные настройки

### Для новых сервисов
1. Добавить зависимость на `common-core`
2. Создать `application.yml` с импортом общих настроек
3. Добавить специфичные настройки

## Совместимость

- ✅ Spring Boot 3.1.9
- ✅ Spring Security 6.1.7
- ✅ Spring Kafka
- ✅ gRPC
- ✅ OAuth2 Authorization Server 1.2.3
