# Kafka Monitoring и Management

Этот документ описывает настройку мониторинга и управления для Apache Kafka в проекте Product Visor.

## Добавленные сервисы

### 1. AKHQ (Kafka UI) - Веб-интерфейс для управления Kafka
- **URL**: http://localhost:8080
- **Описание**: Современный веб-интерфейс для управления Kafka кластером
- **Возможности**:
  - Просмотр и создание топиков
  - Управление схемами (Schema Registry)
  - Мониторинг consumer groups
  - Просмотр сообщений в реальном времени
  - Управление ACL и пользователями

### 2. Schema Registry
- **URL**: http://localhost:8081
- **Описание**: Централизованное управление схемами данных
- **Возможности**:
  - Регистрация и версионирование схем
  - Совместимость схем
  - REST API для управления схемами

### 3. Kafka Connect
- **URL**: http://localhost:8083
- **Описание**: Платформа для интеграции Kafka с внешними системами
- **Возможности**:
  - Создание connectors для различных источников данных
  - Управление через REST API
  - Мониторинг статуса connectors

### 4. Kafka Exporter
- **URL**: http://localhost:9308/metrics
- **Описание**: Экспортер метрик Kafka для Prometheus
- **Метрики**:
  - Количество брокеров
  - Количество партиций по топикам
  - Consumer lag
  - Количество сообщений в секунду
  - Статус consumer groups

## Доступные топики

Следующие топики создаются автоматически при запуске:

- `telegram.incoming.messages` - входящие сообщения Telegram
- `telegram.outgoing.messages` - исходящие сообщения Telegram
- `user.events` - события пользователей
- `order-events` - события заказов
- `product-events` - события продуктов

## Мониторинг в Grafana

### Дашборд "Kafka Monitoring"
Доступен в Grafana по адресу: http://localhost:3000

**Панели мониторинга:**
1. **Kafka Brokers** - количество активных брокеров
2. **Topic Partitions** - количество партиций по топикам
3. **Messages per Second** - скорость обработки сообщений
4. **Consumer Group Members** - количество участников consumer groups
5. **Consumer Lag** - отставание потребителей
6. **Topic Partition Leaders** - лидеры партиций

## Использование AKHQ

### Просмотр топиков
1. Откройте http://localhost:8080
2. В левом меню выберите "Topics"
3. Выберите нужный топик для просмотра деталей

### Просмотр сообщений
1. В разделе Topics выберите топик
2. Перейдите на вкладку "Data"
3. Настройте фильтры и нажмите "Search"

### Управление Consumer Groups
1. В левом меню выберите "Consumer Groups"
2. Просмотрите активные группы
3. Проверьте lag и статус потребителей

### Работа со Schema Registry
1. В левом меню выберите "Schema Registry"
2. Просматривайте и управляйте схемами
3. Проверяйте совместимость версий

## REST API

### Schema Registry API
```bash
# Получить список схем
curl http://localhost:8081/subjects

# Получить схему по ID
curl http://localhost:8081/schemas/ids/{schema-id}

# Создать новую схему
curl -X POST http://localhost:8081/subjects/{subject}/versions \
  -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  -d '{"schema": "{\"type\":\"string\"}"}'
```

### Kafka Connect API
```bash
# Получить список connectors
curl http://localhost:8083/connectors

# Создать connector
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "my-connector",
    "config": {
      "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
      "tasks.max": "1",
      "connection.url": "jdbc:postgresql://localhost:5432/mydb",
      "topic.prefix": "my-topic"
    }
  }'
```

## Метрики Prometheus

Kafka Exporter предоставляет следующие метрики:

- `kafka_brokers` - количество брокеров
- `kafka_topic_partitions` - количество партиций по топикам
- `kafka_topic_partition_current_offset` - текущий offset партиций
- `kafka_consumer_group_members` - количество участников consumer groups
- `kafka_consumer_lag_sum` - общий lag потребителей
- `kafka_topic_partition_leader` - лидеры партиций

## Troubleshooting

### Проблемы с подключением к Kafka
1. Проверьте, что Zookeeper запущен: `docker-compose ps zookeeper`
2. Проверьте логи Kafka: `docker-compose logs kafka`
3. Убедитесь, что порты 9092 и 29092 доступны

### Проблемы с AKHQ
1. Проверьте логи: `docker-compose logs akhq`
2. Убедитесь, что Kafka доступен по адресу `kafka:29092`
3. Проверьте конфигурацию в переменной `AKHQ_CONFIGURATION`

### Проблемы с Schema Registry
1. Проверьте логи: `docker-compose logs schema-registry`
2. Убедитесь, что Kafka доступен
3. Проверьте переменную `SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS`

## Команды для управления

```bash
# Запуск всех сервисов
docker-compose up -d

# Остановка всех сервисов
docker-compose down

# Просмотр логов Kafka
docker-compose logs -f kafka

# Просмотр логов AKHQ
docker-compose logs -f akhq

# Перезапуск Kafka
docker-compose restart kafka

# Создание нового топика
docker-compose exec kafka kafka-topics --bootstrap-server kafka:29092 \
  --create --topic my-new-topic --partitions 3 --replication-factor 1
```

## Безопасность

В текущей конфигурации используется PLAINTEXT протокол без аутентификации. Для продакшена рекомендуется:

1. Настроить SSL/TLS
2. Включить SASL аутентификацию
3. Настроить ACL для контроля доступа
4. Использовать secrets для хранения паролей
