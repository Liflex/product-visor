from __future__ import annotations
import os
from pydantic_settings import BaseSettings, SettingsConfigDict
from pydantic import Field, ValidationError


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    telegram_bot_token: str = Field(default="", alias="BOT_TOKEN")
    kafka_bootstrap_servers: str = Field(default="localhost:9092", alias="KAFKA_BOOTSTRAP_SERVERS")
    kafka_topic_outgoing: str = Field(default="telegram.outgoing.messages", alias="KAFKA_TOPIC_OUTGOING")
    kafka_consumer_group: str = Field(default="python-bot", alias="KAFKA_CONSUMER_GROUP")
    kafka_topic_user_events: str = Field(default="user.events", alias="KAFKA_TOPIC_USER_EVENTS")
    kafka_topic_order_events: str = Field(default="order-events", alias="KAFKA_TOPIC_ORDER_EVENTS")
    metrics_port: int = Field(default=9101, alias="METRICS_PORT")

    def validate_bot_token(self) -> bool:
        """Проверяет, что токен бота настроен и не пустой"""
        return bool(self.telegram_bot_token and self.telegram_bot_token != "your_telegram_bot_token_here")


try:
    settings = Settings()
except ValidationError as e:
    print(f"❌ Ошибка конфигурации: {e}")
    print("📝 Убедитесь, что файл .env создан и содержит правильные значения")
    exit(1)



