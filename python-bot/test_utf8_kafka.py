#!/usr/bin/env python3
"""
Скрипт для тестирования UTF-8 кодировки в Kafka
"""
import asyncio
import json
import sys
from aiokafka import AIOKafkaProducer, AIOKafkaConsumer

# Настройка кодировки
sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')

async def test_utf8_producer():
    """Тестирует отправку сообщений с русскими символами в Kafka"""
    
    # Тестовое сообщение с русскими символами
    test_message = {
        "eventType": "ORDER_CREATED",
        "postingNumber": "TEST-UTF8-001",
        "source": "OZON",
        "eventTime": "2024-01-20T10:30:00Z",
        "totalPrice": "1250.50",
        "orderName": "Контактные линзы Acuvue Oasys 1-Day",
        "items": [
            {
                "id": 1,
                "product_id": 101,
                "offer_id": "ACU001",
                "name": "Контактные линзы Acuvue Oasys 1-Day",
                "quantity": 2,
                "price": "625.25",
                "sku": "ACU001-30"
            }
        ]
    }
    
    print("🚀 Тестирование UTF-8 кодировки в Kafka")
    print("=" * 50)
    print(f"📤 Отправляем сообщение: {json.dumps(test_message, ensure_ascii=False, indent=2)}")
    
    # Создаем producer с правильной кодировкой
    producer = AIOKafkaProducer(
        bootstrap_servers="localhost:9092",
        value_serializer=lambda v: json.dumps(v, ensure_ascii=False, separators=(',', ':')).encode("utf-8"),
        key_serializer=lambda k: k.encode("utf-8") if k else None,
    )
    
    try:
        await producer.start()
        print("✅ Producer запущен")
        
        # Отправляем сообщение
        await producer.send_and_wait("order-events", test_message, key="TEST-UTF8-001")
        print("✅ Сообщение отправлено в топик order-events")
        
        # Ждем немного
        await asyncio.sleep(2)
        
    except Exception as e:
        print(f"❌ Ошибка при отправке: {e}")
    finally:
        await producer.stop()
        print("🛑 Producer остановлен")

async def test_utf8_consumer():
    """Тестирует получение сообщений с русскими символами из Kafka"""
    
    print("\n📥 Тестирование получения сообщений...")
    
    # Создаем consumer с правильной кодировкой
    consumer = AIOKafkaConsumer(
        "order-events",
        bootstrap_servers="localhost:9092",
        group_id="test-utf8-group",
        enable_auto_commit=True,
        value_deserializer=lambda v: json.loads(v.decode("utf-8")),
        key_deserializer=lambda k: k.decode("utf-8") if k else None,
        auto_offset_reset="latest"
    )
    
    try:
        await consumer.start()
        print("✅ Consumer запущен")
        
        # Ждем сообщения
        print("⏳ Ожидаем сообщения (30 секунд)...")
        
        async for msg in consumer:
            print(f"📨 Получено сообщение:")
            print(f"   Key: {msg.key}")
            print(f"   Value: {json.dumps(msg.value, ensure_ascii=False, indent=2)}")
            print(f"   Кодировка orderName: {repr(msg.value.get('orderName', ''))}")
            break  # Получаем только одно сообщение
            
    except Exception as e:
        print(f"❌ Ошибка при получении: {e}")
    finally:
        await consumer.stop()
        print("🛑 Consumer остановлен")

async def main():
    """Основная функция"""
    print("🔍 Проверка UTF-8 кодировки в Kafka")
    print("=" * 50)
    
    # Тестируем producer
    await test_utf8_producer()
    
    # Тестируем consumer
    await test_utf8_consumer()
    
    print("\n✅ Тестирование завершено")

if __name__ == "__main__":
    asyncio.run(main())

