#!/usr/bin/env python3
"""
Скрипт для создания .env файла с настройками бота
"""
import os
import sys

def create_env_file():
    """Создает .env файл с настройками по умолчанию"""
    
    env_content = """# Telegram Bot Configuration
# Получите токен у @BotFather в Telegram
BOT_TOKEN=your_telegram_bot_token_here

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_TOPIC_OUTGOING=telegram.outgoing.messages
KAFKA_CONSUMER_GROUP=python-bot
KAFKA_TOPIC_USER_EVENTS=user.events
KAFKA_TOPIC_ORDER_EVENTS=order-events

# Metrics Configuration
METRICS_PORT=9101
"""
    
    env_file_path = os.path.join(os.path.dirname(__file__), '.env')
    
    if os.path.exists(env_file_path):
        print(f"⚠️  Файл .env уже существует: {env_file_path}")
        response = input("Хотите перезаписать? (y/N): ")
        if response.lower() != 'y':
            print("❌ Отменено")
            return False
    
    try:
        with open(env_file_path, 'w', encoding='utf-8') as f:
            f.write(env_content)
        
        print(f"✅ Файл .env создан: {env_file_path}")
        print("\n📝 Следующие шаги:")
        print("1. Получите токен бота у @BotFather в Telegram")
        print("2. Отредактируйте файл .env и замените 'your_telegram_bot_token_here' на ваш токен")
        print("3. Запустите бота: python run_bot.py")
        
        return True
        
    except Exception as e:
        print(f"❌ Ошибка при создании .env файла: {e}")
        return False

def check_dependencies():
    """Проверяет установленные зависимости"""
    required_packages = [
        'aiogram',
        'aiokafka', 
        'prometheus_client',
        'pydantic_settings'
    ]
    
    missing_packages = []
    
    for package in required_packages:
        try:
            __import__(package.replace('-', '_'))
        except ImportError:
            missing_packages.append(package)
    
    if missing_packages:
        print("❌ Отсутствуют зависимости:")
        for package in missing_packages:
            print(f"   - {package}")
        print("\n📦 Установите их командой:")
        print(f"   pip install {' '.join(missing_packages)}")
        return False
    else:
        print("✅ Все зависимости установлены")
        return True

if __name__ == "__main__":
    print("🤖 Настройка Telegram Bot")
    print("=" * 50)
    
    # Проверяем зависимости
    if not check_dependencies():
        sys.exit(1)
    
    print("\n📝 Создание .env файла...")
    if create_env_file():
        print("\n🎉 Настройка завершена!")
    else:
        print("\n❌ Настройка не завершена")
        sys.exit(1)

