# 🚀 Быстрый старт Telegram Bot

## Шаг 1: Установка зависимостей
```bash
cd python-bot
pip install -r requirements.txt
```

## Шаг 2: Настройка токена бота
```bash
python setup_env.py
```

## Шаг 3: Получение токена от @BotFather
1. Откройте Telegram
2. Найдите @BotFather
3. Отправьте `/newbot`
4. Следуйте инструкциям
5. Скопируйте полученный токен

## Шаг 4: Настройка .env файла
Отредактируйте файл `.env` и замените:
```env
BOT_TOKEN=ваш_реальный_токен_здесь
```

## Шаг 5: Запуск инфраструктуры
```bash
cd ../infra
docker-compose up -d
```

## Шаг 6: Запуск бота

### В PyCharm:
1. Откройте файл `run_bot.py`
2. Правый клик → "Run 'run_bot'"

### В командной строке:
```bash
cd python-bot
python run_bot.py
```

## Проверка работы
1. Найдите вашего бота в Telegram
2. Отправьте `/start`
3. Должно прийти приветственное сообщение

## Устранение проблем

### "Token is invalid!"
- Убедитесь, что токен скопирован полностью
- Проверьте, что в .env файле нет лишних пробелов

### "ModuleNotFoundError"
```bash
pip install aiogram aiokafka prometheus-client pydantic-settings
```

### "Connection refused" (Kafka)
```bash
cd infra
docker-compose up -d
```

## Полезные ссылки
- Метрики: http://localhost:9101/metrics
- Документация: ../TELEGRAM_BOT_INTEGRATION.md

