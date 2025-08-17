# Telegram Bot Integration

## Обзор

Система интеграции с Telegram ботом для отправки уведомлений о заказах и управления пользователями.

## Архитектура

### Компоненты

1. **Python Bot** (`python-bot/`) - Telegram бот на aiogram
2. **Client Service** - Java сервис для управления пользователями
3. **Order Service** - Java сервис для обработки заказов
4. **Kafka** - обмен сообщениями между сервисами

### Потоки событий

#### Регистрация пользователя
1. Пользователь отправляет `/start` в Telegram
2. Python бот создает событие `StartCommand` и отправляет в топик `user.events`
3. `UserEventsConsumer` в client service обрабатывает событие
4. `TelegramClientService` создает нового пользователя или отправляет приветствие
5. Отправляется сообщение в топик `telegram.outgoing.messages`
6. Python бот получает сообщение и отправляет его пользователю

#### Уведомления о заказах
1. Order Service создает/отменяет заказ
2. Отправляется событие в топик `order-events`
3. `OrderEventsConsumer` в client service обрабатывает событие
4. Отправляются уведомления всем зарегистрированным пользователям
5. Python бот доставляет сообщения пользователям

## Настройка

### 1. Создание Telegram бота

1. Обратитесь к @BotFather в Telegram
2. Создайте нового бота командой `/newbot`
3. Получите токен бота

### 2. Настройка переменных окружения

Создайте файл `.env` в папке `python-bot/`:

```env
BOT_TOKEN=your_telegram_bot_token_here
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_TOPIC_OUTGOING=telegram.outgoing.messages
KAFKA_CONSUMER_GROUP=python-bot
KAFKA_TOPIC_USER_EVENTS=user.events
KAFKA_TOPIC_ORDER_EVENTS=order-events
METRICS_PORT=9101
```

### 3. Запуск сервисов

#### Запуск инфраструктуры
```bash
cd infra
docker-compose up -d
```

#### Запуск Java сервисов
```bash
# Client Service
cd client
mvn spring-boot:run

# Order Service  
cd ../order-service
mvn spring-boot:run
```

#### Запуск Python бота

**Вариант 1: Через командную строку**
```bash
cd python-bot
python run_bot.py
```

**Вариант 2: Через PyCharm**

1. Откройте проект в PyCharm
2. Перейдите в папку `python-bot`
3. Правый клик на файл `run_bot.py` → "Run 'run_bot'"
4. Или создайте конфигурацию запуска:
   - File → Settings → Project → Python Interpreter
   - Убедитесь, что выбран Python 3.11+
   - Run → Edit Configurations → + → Python
   - Script path: `python-bot/run_bot.py`
   - Working directory: `python-bot`
   - Environment variables: добавьте переменные из `.env` файла

**Вариант 3: Через модуль**
```bash
cd python-bot
python -m bot
```

## API Endpoints

### Client Service

- `GET /api/v1/telegram-client/exists/{chatId}` - проверить существование пользователя

## Шаблоны сообщений

Шаблоны сообщений находятся в `python-bot/bot/templates/messages.yaml`:

- `welcome` - приветствие нового пользователя
- `welcome_back` - приветствие существующего пользователя
- `user.registered` - подтверждение регистрации
- `order.created` - уведомление о новом заказе (с названием товара и стоимостью)
- `order.cancelled` - уведомление об отмене заказа (с названием товара и стоимостью)

## Kafka топики

- `user.events` - события пользователей (StartCommand, UserRegistrationSubmit)
- `order-events` - события заказов (ORDER_CREATED, ORDER_CANCELLED)
- `telegram.outgoing.messages` - исходящие сообщения в Telegram

## Мониторинг

### Метрики Python бота
- `http://localhost:9101/metrics` - Prometheus метрики

### Метрики Java сервисов
- `http://localhost:9088/actuator/prometheus` - Client Service метрики
- `http://localhost:9088/actuator/prometheus` - Order Service метрики

## Тестирование

### Тест команды /start
1. Найдите вашего бота в Telegram
2. Отправьте команду `/start`
3. Проверьте, что получено приветственное сообщение
4. Проверьте в логах client service создание пользователя

### Тест уведомлений о заказах
1. Создайте заказ через API order service
2. Проверьте получение уведомления в Telegram
3. Проверьте логи OrderEventsConsumer

## Troubleshooting

### Проблемы с подключением к Kafka
- Убедитесь, что Kafka запущен: `docker-compose ps`
- Проверьте настройки `KAFKA_BOOTSTRAP_SERVERS`

### Проблемы с Telegram API
- Проверьте правильность токена бота
- Убедитесь, что бот не заблокирован пользователем

### Проблемы с базой данных
- Проверьте подключение к PostgreSQL
- Убедитесь, что миграции выполнены успешно
