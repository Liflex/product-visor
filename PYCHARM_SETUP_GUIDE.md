# PyCharm Setup Guide для Telegram Bot

## Подготовка к запуску

### 1. Установка зависимостей

Убедитесь, что установлены все зависимости:
```bash
cd python-bot
pip install -r requirements.txt
```

Если файла requirements.txt нет, установите зависимости вручную:
```bash
pip install aiogram aiokafka prometheus-client pydantic-settings
```

### 2. Создание файла .env

Создайте файл `.env` в папке `python-bot/` на основе `env_example.txt`:

```env
# Telegram Bot Configuration
BOT_TOKEN=your_actual_bot_token_here

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_TOPIC_OUTGOING=telegram.outgoing.messages
KAFKA_CONSUMER_GROUP=python-bot
KAFKA_TOPIC_USER_EVENTS=user.events
KAFKA_TOPIC_ORDER_EVENTS=order-events

# Metrics Configuration
METRICS_PORT=9101
```

## Настройка PyCharm

### Вариант 1: Простой запуск

1. Откройте проект в PyCharm
2. Перейдите в папку `python-bot`
3. Найдите файл `run_bot.py`
4. Правый клик на файл → "Run 'run_bot'"

### Вариант 2: Создание конфигурации запуска

1. **Откройте настройки конфигураций:**
   - Run → Edit Configurations...
   - Или нажмите `Ctrl+Shift+A` → "Edit Configurations"

2. **Создайте новую конфигурацию:**
   - Нажмите `+` → Python

3. **Настройте параметры:**
   - **Name:** `Telegram Bot`
   - **Script path:** `C:\Users\TaoBao\IdeaProjects\UnicProject\product-visor\python-bot\run_bot.py`
   - **Working directory:** `C:\Users\TaoBao\IdeaProjects\UnicProject\product-visor\python-bot`
   - **Python interpreter:** Выберите Python 3.11+

4. **Добавьте переменные окружения:**
   - В разделе "Environment variables" нажмите `...`
   - Добавьте переменные:
     ```
     BOT_TOKEN=your_actual_bot_token_here
     KAFKA_BOOTSTRAP_SERVERS=localhost:9092
     KAFKA_TOPIC_OUTGOING=telegram.outgoing.messages
     KAFKA_CONSUMER_GROUP=python-bot
     KAFKA_TOPIC_USER_EVENTS=user.events
     KAFKA_TOPIC_ORDER_EVENTS=order-events
     METRICS_PORT=9101
     ```

5. **Сохраните конфигурацию:**
   - Нажмите "OK"

### Вариант 3: Запуск через модуль

1. Создайте конфигурацию Python
2. **Script path:** оставьте пустым
3. **Module name:** `bot`
4. **Working directory:** `python-bot`
5. Добавьте переменные окружения как в варианте 2

## Проверка запуска

### 1. Убедитесь, что инфраструктура запущена:
```bash
cd infra
docker-compose up -d
```

### 2. Проверьте, что Kafka доступен:
```bash
# Проверьте, что порт 9092 открыт
netstat -an | findstr 9092
```

### 3. Запустите бота в PyCharm:
- Нажмите зеленую кнопку "Run" или `Shift+F10`
- Проверьте логи в консоли PyCharm

### 4. Ожидаемые сообщения при успешном запуске:
```
INFO:bot:✅ Sent StartCommand event for chat_id: 123456789
INFO:bot:🚀 OrderEventProducer initialized and ready to send events to topic: order-events
```

## Troubleshooting

### Ошибка "ImportError: attempted relative import"
**Решение:** Используйте файл `run_bot.py` вместо прямого запуска `__main__.py`

### Ошибка "ModuleNotFoundError: No module named 'aiogram'"
**Решение:** Установите зависимости:
```bash
pip install aiogram aiokafka prometheus-client pydantic-settings
```

### Ошибка подключения к Kafka
**Решение:** 
1. Убедитесь, что Docker запущен
2. Запустите инфраструктуру: `docker-compose up -d`
3. Проверьте переменную `KAFKA_BOOTSTRAP_SERVERS`

### Ошибка "Token is invalid!"
**Решение:** 
1. **Автоматическая настройка:**
   ```bash
   cd python-bot
   python setup_env.py
   ```

2. **Ручная настройка:**
   - Создайте файл `.env` в папке `python-bot/`
   - Добавьте ваш токен:
     ```env
     BOT_TOKEN=1234567890:ABCdefGHIjklMNOpqrsTUVwxyz
     ```

3. **Получение токена:**
   - Обратитесь к @BotFather в Telegram
   - Отправьте команду `/newbot`
   - Следуйте инструкциям
   - Скопируйте полученный токен

4. **В PyCharm:**
   - Обновите переменную `BOT_TOKEN` в конфигурации запуска
   - Или создайте файл `.env` в папке `python-bot/`

## Полезные советы

1. **Отладка:** Поставьте breakpoint в `bot/main.py` для отладки
2. **Логи:** Все логи выводятся в консоль PyCharm
3. **Перезапуск:** При изменении кода просто перезапустите конфигурацию
4. **Метрики:** Откройте `http://localhost:9101/metrics` для просмотра метрик
