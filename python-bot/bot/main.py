from __future__ import annotations

import asyncio
import contextlib
import json
import logging
import sys
import locale
from datetime import datetime, timezone
from prometheus_client import start_http_server, Counter, Histogram
from aiogram import Bot, Dispatcher, types
from aiogram.client.default import DefaultBotProperties
from aiogram.enums import ParseMode
from aiogram.filters import Command
from .config import settings
from .kafka_bus import kafka_bus
from .renderer import render_from_body

# Настройка кодировки для корректной работы с русскими символами
sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')

# Устанавливаем локаль для корректной работы с UTF-8
try:
    locale.setlocale(locale.LC_ALL, 'en_US.UTF-8')
except locale.Error:
    try:
        locale.setlocale(locale.LC_ALL, 'C.UTF-8')
    except locale.Error:
        pass  # Используем системную локаль по умолчанию

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    encoding='utf-8'
)
logger = logging.getLogger("python-bot")
# Снижаем уровень логирования aiokafka, чтобы не засорять лог временными ошибками координатора
logging.getLogger("aiokafka").setLevel(logging.WARNING)

REQUESTS_COUNTER = Counter("bot_events_total", "Count of processed bot events", ["type"])
REQUEST_DURATION = Histogram("bot_event_duration_seconds", "Event processing duration", ["type"])


async def handle_start_command(message: types.Message) -> None:
    """Handle /start command from user"""
    try:
        # Create StartCommand event with properly formatted datetime
        start_event = {
            "type": "StartCommand",
            "chatId": message.chat.id,
            "botId": message.from_user.id,
            "username": message.from_user.username,
            "firstName": message.from_user.first_name,
            "lastName": message.from_user.last_name,
            "eventTime": datetime.now(timezone).strftime("%Y-%m-%dT%H:%M:%S.%f")[:-3] + "Z"
        }

        # Send event to user.events topic
        await kafka_bus.produce("user.events", start_event, str(message.chat.id))

        logger.info("✅ Sent StartCommand event for chat_id: %s", message.chat.id)
        REQUESTS_COUNTER.labels("start_command").inc()

    except Exception as e:
        logger.exception("Failed to handle start command for chat %s: %s", message.chat.id, e)


async def outgoing_bridge(bot: Bot) -> None:
    async for key, payload in kafka_bus.consume_outgoing():
        chat_id = payload.get("chat_id")
        text = payload.get("text")
        body = payload.get("body")
        if chat_id and body and isinstance(body, dict):
            try:
                with REQUEST_DURATION.labels("outgoing_body").time():
                    await bot.send_message(chat_id=chat_id, text=render_from_body(body))
                REQUESTS_COUNTER.labels("outgoing_body").inc()
            except Exception as e:
                logger.exception("Failed to send message to chat %s: %s", chat_id, e)
            continue
        if chat_id and text:
            try:
                with REQUEST_DURATION.labels("outgoing_text").time():
                    await bot.send_message(chat_id=chat_id, text=text)
                REQUESTS_COUNTER.labels("outgoing_text").inc()
            except Exception as e:
                logger.exception("Failed to send message to chat %s: %s", chat_id, e)


async def main_async() -> None:
    # Проверяем токен бота перед запуском
    if not settings.validate_bot_token():
        logger.error("❌ Bot token is not configured!")
        logger.error("📝 Please create .env file with BOT_TOKEN=your_actual_token")
        logger.error("📝 Or set BOT_TOKEN environment variable")
        return

    logger.info("🚀 Starting Telegram Bot...")
    logger.info("📊 Metrics server will be available at http://localhost:%d/metrics", settings.metrics_port)

    await kafka_bus.start()
    bot = Bot(token=settings.telegram_bot_token, default=DefaultBotProperties(parse_mode=ParseMode.HTML))
    dp = Dispatcher()

    # Register command handlers
    dp.message.register(handle_start_command, Command("start"))

    bridge_task = asyncio.create_task(outgoing_bridge(bot))
    try:
        logger.info("✅ Bot started successfully!")
        await dp.start_polling(bot)
    finally:
        bridge_task.cancel()
        with contextlib.suppress(Exception):
            await bridge_task
        await kafka_bus.stop()


def main() -> None:
    try:
        start_http_server(settings.metrics_port)
        asyncio.run(main_async())
    except KeyboardInterrupt:
        logger.info("🛑 Bot stopped by user")
    except Exception as e:
        logger.error("❌ Bot failed to start: %s", e)