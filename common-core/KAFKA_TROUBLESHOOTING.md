# Устранение проблем с Kafka

## Проблема: Конфликт бинов KafkaTemplate

### Ошибка
```
The bean 'kafkaTemplate', defined in class path resource [ru/dmitartur/common/config/KafkaCommonConfig.class], could not be registered. A bean with that name has already been defined in class path resource [org/springframework/boot/autoconfigure/kafka/KafkaAutoConfiguration.class] and overriding is disabled.
```

### Причина
Spring Boot автоматически создает `KafkaTemplate` через `KafkaAutoConfiguration`, а наш `KafkaCommonConfig` пытается создать еще один с тем же именем.

### Решение
1. **Используйте `@ConditionalOnMissingBean`** в `KafkaCommonConfig`
2. **Переименуйте бины** в `KafkaCommonConfig`
3. **Используйте правильные настройки** в `application.yml`

### Текущая конфигурация

#### KafkaCommonConfig
```java
@Configuration
public class KafkaCommonConfig {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() { 
        return new ObjectMapper(); 
    }

    @Bean
    @ConditionalOnMissingBean(name = "stringProducerFactory")
    public ProducerFactory<String, String> stringProducerFactory() {
        // Конфигурация для строковых сериализаторов
    }

    @Bean
    @ConditionalOnMissingBean(name = "stringKafkaTemplate")
    public KafkaTemplate<String, String> stringKafkaTemplate() { 
        return new KafkaTemplate<>(stringProducerFactory()); 
    }
}
```

#### Настройки в application.yml
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

### Использование в коде

#### Основной KafkaTemplate (создается Spring Boot)
```java
@Service
public class MyService {
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    public void sendMessage() {
        kafkaTemplate.send("topic", "message");
    }
}
```

#### Дополнительный KafkaTemplate (если нужен)
```java
@Service
public class MyService {
    
    @Autowired
    @Qualifier("stringKafkaTemplate")
    private KafkaTemplate<String, String> stringKafkaTemplate;
    
    public void sendStringMessage() {
        stringKafkaTemplate.send("topic", "message");
    }
}
```

## Другие проблемы

### 1. Неправильные настройки в application.yml
**Проблема**: Настройки Kafka находятся под ключом `kafka` вместо `spring.kafka`

**Решение**: Переместите настройки под правильный ключ:
```yaml
# ❌ Неправильно
kafka:
  bootstrap-servers: localhost:9092

# ✅ Правильно
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

### 2. Дублирующие конфигурации
**Проблема**: В разных сервисах есть одинаковые настройки Kafka

**Решение**: Используйте общие настройки из `common-core`:
```yaml
# В common-core/src/main/resources/application-common.yml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    # ... общие настройки

# В сервисах - только специфичные настройки
kafka:
  topics:
    order-events: my-service-events
```

### 3. Отсутствие зависимостей
**Проблема**: Сервис не может найти классы Kafka

**Решение**: Добавьте зависимость в `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

## Проверка конфигурации

### 1. Проверьте, что Kafka запущена
```bash
# Проверьте, что Kafka доступна
telnet localhost 9092
```

### 2. Проверьте логи Spring Boot
```bash
# Ищите сообщения о Kafka
grep -i kafka application.log
```

### 3. Проверьте бины в контексте
```java
@Autowired
private ApplicationContext context;

public void checkKafkaBeans() {
    String[] beanNames = context.getBeanDefinitionNames();
    for (String beanName : beanNames) {
        if (beanName.toLowerCase().contains("kafka")) {
            System.out.println("Found Kafka bean: " + beanName);
        }
    }
}
```

## Рекомендации

1. **Всегда используйте `@ConditionalOnMissingBean`** для дополнительных бинов
2. **Разделяйте настройки**: `spring.kafka` для Spring Boot, `kafka` для сервисов
3. **Используйте общие настройки** из `common-core`
4. **Проверяйте логи** при запуске сервисов
5. **Тестируйте подключение** к Kafka перед запуском приложения
