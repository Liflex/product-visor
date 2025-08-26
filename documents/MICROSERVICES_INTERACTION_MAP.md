# Product Visor - Карта взаимодействий микросервисов

## 🏗️ Архитектура системы

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Telegram      │    │   Mobile App    │
│   (React)       │    │   Bot           │    │   (Future)      │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────┴─────────────┐
                    │                           │
            ┌───────▼────────┐        ┌────────▼───────┐
            │   API Gateway  │        │   Load         │
            │   (Nginx)      │        │   Balancer     │
            └───────┬────────┘        └────────┬───────┘
                    │                          │
        ┌───────────┴───────────┬──────────────┴───────────┐
        │                       │                          │
┌───────▼────────┐    ┌────────▼───────┐        ┌────────▼───────┐
│ Authorization  │    │ Product Visor  │        │ Client Service │
│ Service        │    │ Backend        │        │                │
└───────┬────────┘    └────────┬───────┘        └────────┬───────┘
        │                      │                         │
        └──────────────────────┼─────────────────────────┘
                               │
                    ┌───────────┴───────────┐
                    │                       │
            ┌───────▼────────┐    ┌────────▼───────┐
            │   Kafka        │    │   PostgreSQL   │
            │   (Message     │    │   (Database)   │
            │    Broker)     │    │                │
            └───────┬────────┘    └────────────────┘
                    │
        ┌───────────┴───────────┐
        │                       │
┌───────▼────────┐    ┌────────▼───────┐
│ Ozon Service   │    │ Yandex Service │
│                │    │                │
└────────────────┘    └────────────────┘
```

## 🔄 Детальная карта взаимодействий

### 📡 HTTP API взаимодействия

#### 1. Frontend ↔ Backend Services
```
Frontend (React) ←→ Product Visor Backend
├── GET    /api/v1/product          # Получение списка товаров
├── POST   /api/v1/product          # Создание товара
├── PUT    /api/v1/product/{id}     # Обновление товара
├── DELETE /api/v1/product/{id}     # Удаление товара
├── GET    /api/v1/category         # Получение категорий
├── POST   /api/v1/stock-sync/{mp}  # Синхронизация остатков
└── GET    /api/v1/image/{filename} # Получение изображений

Frontend (React) ←→ Authorization Service
├── POST   /auth/login              # Авторизация
├── POST   /auth/register           # Регистрация
├── POST   /auth/refresh            # Обновление токена
└── GET    /auth/profile            # Профиль пользователя

Frontend (React) ←→ Client Service
├── GET    /api/v1/company          # Получение компаний
├── POST   /api/v1/company          # Создание компании
├── PUT    /api/v1/company/{id}     # Обновление компании
└── GET    /api/v1/profile          # Профиль пользователя
```

#### 2. Telegram Bot ↔ Services
```
Telegram Bot ←→ Product Visor Backend
├── GET    /api/v1/product          # Получение товаров для уведомлений
├── GET    /api/v1/orders           # Получение заказов
└── POST   /api/v1/notifications    # Отправка уведомлений

Telegram Bot ←→ Authorization Service
└── POST   /auth/validate           # Валидация токенов
```

### 📨 Kafka взаимодействия

#### Топики для синхронизации остатков
```
Topic: stock-sync
├── Producer: Product Visor Backend
├── Consumers: Ozon Service, Yandex Service
├── Message: StockSyncRequest
└── Routing: По полю marketplace

Topic: stock-sync-response
├── Producers: Ozon Service, Yandex Service
├── Consumer: Product Visor Backend
├── Message: StockSyncResponse
└── Routing: По полю marketplace
```

#### Структура сообщений Kafka

**StockSyncRequest:**
```json
{
  "companyId": "uuid",
  "marketplace": "OZON|YANDEX",
  "items": [
    {
      "offerId": "string",
      "sku": "string", 
      "quantity": "integer",
      "productId": "string"
    }
  ],
  "warehouseId": "string"
}
```

**StockSyncResponse:**
```json
{
  "marketplace": "OZON|YANDEX",
  "status": "SUCCESS|FAILED|PARTIAL_SUCCESS",
  "processedAt": "datetime",
  "totalItems": "integer",
  "successCount": "integer",
  "failedCount": "integer",
  "errorMessage": "string"
}
```

### 🗄️ База данных

#### PostgreSQL схемы
```
product_visor_db
├── products           # Товары
├── categories         # Категории товаров
├── product_attributes # Атрибуты товаров
├── product_history    # История изменений
├── images             # Изображения товаров
└── markets            # Маркетплейсы

authorization_db
├── users              # Пользователи
├── roles              # Роли
├── permissions        # Права доступа
└── user_sessions      # Сессии пользователей

client_db
├── companies          # Компании
├── company_credentials # Учетные данные для маркетплейсов
├── user_profiles      # Профили пользователей
└── company_avatars    # Аватары компаний
```

## 📊 Логирование и мониторинг

### 📝 Структура логов

#### Уровни логирования
```
ERROR   ❌ - Критические ошибки, требующие немедленного внимания
WARN    ⚠️  - Предупреждения, потенциальные проблемы
INFO    ℹ️  - Информационные сообщения о работе системы
DEBUG   🔍 - Детальная отладочная информация
TRACE   📋 - Максимально детальная информация
```

#### Формат логов
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "level": "INFO",
  "service": "product-visor-backend",
  "traceId": "abc123-def456",
  "userId": "user-uuid",
  "companyId": "company-uuid",
  "message": "Product created successfully",
  "data": {
    "productId": "123",
    "productName": "Contact Lens XYZ"
  }
}
```

### 📊 Метрики мониторинга

#### Ключевые метрики
```
API Performance:
├── Response Time (avg, p95, p99)
├── Request Rate (RPS)
├── Error Rate (%)
└── Availability (%)

Kafka Metrics:
├── Message Throughput
├── Consumer Lag
├── Producer Latency
└── Topic Size

Database Metrics:
├── Query Performance
├── Connection Pool Usage
├── Lock Wait Time
└── Cache Hit Rate

Business Metrics:
├── Products Created/Updated
├── Sync Success Rate
├── User Activity
└── Marketplace Integration Status
```

## 🔐 Безопасность

### Аутентификация и авторизация
```
JWT Tokens:
├── Access Token (15 min)
├── Refresh Token (7 days)
└── Company Context Token

OAuth2 Flows:
├── Authorization Code (Web)
├── Client Credentials (Service-to-Service)
└── Resource Owner Password (Legacy)

API Security:
├── Rate Limiting
├── CORS Configuration
├── Input Validation
└── SQL Injection Protection
```

### Шифрование данных
```
Sensitive Data:
├── User Passwords (bcrypt)
├── API Keys (AES-256)
├── Database Connections (SSL/TLS)
└── Kafka Messages (SSL/TLS)
```

## 🚀 Масштабирование

### Горизонтальное масштабирование
```
Load Balancing:
├── Nginx (API Gateway)
├── Round Robin
├── Least Connections
└── Health Checks

Database Scaling:
├── Read Replicas
├── Connection Pooling
├── Query Optimization
└── Caching (Redis)

Kafka Scaling:
├── Multiple Partitions
├── Consumer Groups
├── Topic Replication
└── Broker Clustering
```

### Вертикальное масштабирование
```
Resource Allocation:
├── CPU: 2-8 cores per service
├── Memory: 2-16 GB per service
├── Storage: SSD with RAID
└── Network: 1-10 Gbps
```

## 🔧 Конфигурация

### Environment Variables
```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=product_visor_db
DB_USERNAME=postgres
DB_PASSWORD=secret

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_GROUP_ID=product-visor-group

# Services
AUTH_SERVICE_URL=http://localhost:8081
CLIENT_SERVICE_URL=http://localhost:8082
OZON_SERVICE_URL=http://localhost:8083
YANDEX_SERVICE_URL=http://localhost:8084

# Security
JWT_SECRET=your-secret-key
ENCRYPTION_KEY=your-encryption-key
```

### Health Checks
```
Endpoints:
├── /health (Basic health check)
├── /health/readiness (Readiness probe)
├── /health/liveness (Liveness probe)
└── /metrics (Prometheus metrics)
```

---

*Эта карта взаимодействий обновляется по мере развития архитектуры системы.*
