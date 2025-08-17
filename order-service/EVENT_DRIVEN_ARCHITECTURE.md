# Event-Driven Architecture для Order Service

## Обзор

Order Service теперь использует архитектуру событий Spring для обработки бизнес-логики, связанной с заказами. Это позволяет разделить основную логику создания/обновления заказов от побочных эффектов, таких как обновление остатков товаров.

## Архитектура событий

### 1. События (Events)

#### OrderCreatedEvent
```java
public class OrderCreatedEvent extends ApplicationEvent {
    private final Order order;
}
```
- **Когда публикуется**: После успешного сохранения нового заказа
- **Где публикуется**: `OrderService.save()`
- **Назначение**: Уведомляет о создании нового заказа

#### OrderCancelledEvent
```java
public class OrderCancelledEvent extends ApplicationEvent {
    private final Order order;
}
```
- **Когда публикуется**: При изменении статуса заказа на CANCELLED
- **Где публикуется**: `OrderService.update()`
- **Назначение**: Уведомляет об отмене заказа

### 2. Обработчики событий (Event Listeners)

#### StockUpdateEventListener
```java
@Component
public class StockUpdateEventListener {
    
    @EventListener
    @Async("orderEventExecutor")
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Уменьшает остатки товаров
        // Отправляет событие в Kafka
    }
    
    @EventListener
    @Async("orderEventExecutor")
    public void handleOrderCancelled(OrderCancelledEvent event) {
        // Возвращает остатки товаров
        // Отправляет событие в Kafka
    }
}
```

**Ответственности**:
- Обновление остатков товаров через gRPC
- Отправка событий в Kafka для других сервисов
- Логирование операций

### 3. Асинхронная обработка

```java
@Configuration
@EnableAsync
public class AsyncEventConfig {
    
    @Bean(name = "orderEventExecutor")
    public Executor orderEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("OrderEvent-");
        return executor;
    }
}
```

## Преимущества новой архитектуры

### 1. Разделение ответственности
- **OrderService**: Только основная логика работы с заказами
- **StockUpdateEventListener**: Только обновление остатков
- **OrderEventProducer**: Только отправка в Kafka

### 2. Асинхронность
- Обработка событий происходит в отдельном потоке
- Не блокирует основной поток создания/обновления заказов
- Улучшает производительность

### 3. Расширяемость
- Легко добавлять новые обработчики событий
- Можно обрабатывать события независимо
- Поддержка транзакций

### 4. Тестируемость
- Легко мокать события в тестах
- Можно тестировать обработчики отдельно
- Изолированная логика

## Поток обработки

### Создание заказа
```
1. OrderService.save() 
   ↓
2. Сохранение в БД
   ↓
3. Публикация OrderCreatedEvent
   ↓
4. StockUpdateEventListener.handleOrderCreated()
   ↓
5. Обновление остатков через gRPC
   ↓
6. Отправка события в Kafka
```

### Отмена заказа
```
1. OrderService.update() (статус = CANCELLED)
   ↓
2. Публикация OrderCancelledEvent
   ↓
3. StockUpdateEventListener.handleOrderCancelled()
   ↓
4. Возврат остатков через gRPC
   ↓
5. Отправка события в Kafka
```

## Конфигурация

### application.yml
```yaml
# Асинхронная обработка событий
spring:
  task:
    execution:
      pool:
        core-size: 2
        max-size: 5
        queue-capacity: 100

# Kafka для событий
kafka:
  enabled: true
  topics:
    order-events: order-events
```

## Добавление новых обработчиков

### Пример: Уведомления
```java
@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    
    @EventListener
    @Async("orderEventExecutor")
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Отправка уведомления о новом заказе
    }
    
    @EventListener
    @Async("orderEventExecutor")
    public void handleOrderCancelled(OrderCancelledEvent event) {
        // Отправка уведомления об отмене заказа
    }
}
```

### Пример: Аналитика
```java
@Component
@RequiredArgsConstructor
public class AnalyticsEventListener {
    
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Сбор аналитических данных
    }
}
```

## Мониторинг

### Логирование
- Все события логируются с эмодзи
- Отдельные логи для успешных/неуспешных операций
- Детальная информация об обновлениях остатков

### Метрики
- Количество обработанных событий
- Время обработки событий
- Ошибки в обработке

## Миграция с предыдущей архитектуры

### Что изменилось:
1. Удален `StockUpdateService`
2. Добавлены `OrderCreatedEvent` и `OrderCancelledEvent`
3. Добавлен `StockUpdateEventListener`
4. Обновлен `OrderService` для публикации событий

### Что осталось:
1. `OrderEventProducer` для Kafka
2. gRPC клиент для обновления остатков
3. Логика фильтрации товаров

## Заключение

Новая архитектура событий обеспечивает:
- ✅ Лучшее разделение ответственности
- ✅ Асинхронную обработку
- ✅ Легкую расширяемость
- ✅ Улучшенную тестируемость
- ✅ Более чистый код
