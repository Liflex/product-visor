# Настройка микросервисной архитектуры Product Visor

## Обзор архитектуры

Product Visor использует микросервисную архитектуру с тремя основными сервисами:

### 1. product-visor-backend (Порт 8085)
- **Назначение**: Основной сервис для управления товарами, категориями, маркетами
- **Схема БД**: `visor`
- **API версия**: `v1`
- **Основные функции**:
  - Управление товарами (CRUD)
  - Управление категориями
  - Управление маркетами
  - Поиск товаров
  - Загрузка изображений

### 2. order-service (Порт 9088)
- **Назначение**: Сервис для управления заказами
- **Схема БД**: `orders`
- **API версия**: нет (прямые эндпоинты)
- **Основные функции**:
  - Создание и управление заказами
  - Фильтрация заказов по маркетам
  - gRPC интеграция с ozon-service
  - Тестовые данные для проверки

### 3. ozon-service (Порт 9097)
- **Назначение**: Интеграция с Ozon API
- **Схема БД**: не использует
- **API версия**: нет (прямые эндпоинты)
- **Основные функции**:
  - Получение заказов из Ozon
  - Backfill заказов
  - gRPC интеграция с order-service

## Конфигурация frontend

### Development прокси (vite.config.js)

В development режиме используется Vite прокси для избежания CORS проблем:

```javascript
server: {
  proxy: {
    '/api/ozon': {
      target: 'http://192.168.1.59:9097',
      changeOrigin: true,
    },
    '/api/orders': {
      target: 'http://192.168.1.59:9088',
      changeOrigin: true,
    }
  }
}
```

### API Configuration (src/config/api-config.js)

```javascript
export const MICROSERVICES = {
  PRODUCT_VISOR_BACKEND: {
    BASE_URL: 'http://localhost:8085',
    API_VERSION: 'v1'
  },
  ORDER_SERVICE: {
    BASE_URL: 'http://localhost:9088',
    API_VERSION: null
  },
  OZON_SERVICE: {
    BASE_URL: 'http://localhost:9097',
    API_VERSION: null
  }
};
```

### Использование в сервисах

```javascript
// Для order-service
import { API_URLS } from '../config/api-config.js';

// Получение всех заказов
const orders = await httpClient.get(API_URLS.ORDERS.BASE);

// Получение заказов по маркету
const ozonOrders = await httpClient.get(API_URLS.ORDERS.BY_MARKET('OZON'));

// Для ozon-service
const backfillResult = await httpClient.post(API_URLS.OZON.ORDERS_FBO_BACKFILL, data);
```

## Запуск сервисов

### 1. Запуск product-visor-backend
```bash
cd product-visor-backend
mvn spring-boot:run
# Сервис доступен на http://localhost:8085
```

### 2. Запуск order-service
```bash
cd order-service
mvn spring-boot:run
# Сервис доступен на http://localhost:9088
```

### 3. Запуск ozon-service
```bash
cd ozon-service
mvn spring-boot:run
# Сервис доступен на http://localhost:9097
```

### 4. Запуск frontend
```bash
npm run dev
# Frontend доступен на http://localhost:5173
```

## Проверка работоспособности

### 1. Проверка product-visor-backend
```bash
curl http://localhost:8085/actuator/health
curl http://localhost:8085/api/v1/product
```

### 2. Проверка order-service
```bash
curl http://localhost:9088/actuator/health
curl http://localhost:9088/api/orders
```

### 3. Проверка ozon-service
```bash
curl http://localhost:9097/actuator/health
```

## Тестовые данные

После запуска order-service автоматически создаются тестовые заказы:

- **Ozon**: `OZON-001-2024-01-15` (COMPLETED)
- **Wildberries**: `WB-002-2024-01-16` (PROCESSING)
- **Yandex Market**: `YM-003-2024-01-17` (SHIPPED)
- **AliExpress**: `AE-004-2024-01-18` (DELIVERED)
- **Other**: `OTHER-005-2024-01-19` (CANCELLED)

## API Endpoints

### product-visor-backend (8085)
- `GET /api/v1/product` - список товаров
- `GET /api/v1/category` - список категорий
- `GET /api/v1/market` - список маркетов

### order-service (9088)
- `GET /api/orders` - список всех заказов
- `GET /api/orders/market/{market}` - заказы по маркету
- `GET /api/orders/{postingNumber}` - заказ по номеру

### ozon-service (9097)
- `POST /api/ozon/orders/fbo/list` - список заказов Ozon
- `POST /api/ozon/orders/fbo/backfill` - backfill заказов

## Мониторинг

Все сервисы предоставляют health check и Prometheus metrics:

- **product-visor-backend**: `http://localhost:8085/actuator/health`
- **order-service**: `http://localhost:9088/actuator/health`
- **ozon-service**: `http://localhost:9097/actuator/health`

## Troubleshooting

### Проблема: 404 при обращении к order-service
**Решение**: Убедитесь, что order-service запущен на порту 9088

### Проблема: CORS ошибки
**Решение**: 
1. Все сервисы уже имеют CORS конфигурацию
2. В development используется Vite прокси для order-service и ozon-service
3. Для production рекомендуется настроить API Gateway

### Проблема: База данных недоступна
**Решение**: Проверьте, что PostgreSQL запущен и доступен на порту 5433

### Проблема: gRPC ошибки
**Решение**: Убедитесь, что все сервисы запущены и gRPC порты свободны
