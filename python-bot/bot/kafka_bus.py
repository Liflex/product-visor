from __future__ import annotations

import json
import logging
from typing import Any, AsyncIterator, Optional
import asyncio

from aiokafka import AIOKafkaProducer, AIOKafkaConsumer

from .config import settings

logger = logging.getLogger(__name__)


class KafkaBus:
    def __init__(self) -> None:
        self._producer: Optional[AIOKafkaProducer] = None
        self._consumer: Optional[AIOKafkaConsumer] = None

    async def start(self) -> None:
        logger.info("Kafka bootstrap_servers=%s", settings.kafka_bootstrap_servers)
        self._producer = AIOKafkaProducer(
            bootstrap_servers=settings.kafka_bootstrap_servers,
            value_serializer=lambda v: json.dumps(v, ensure_ascii=False, separators=(',', ':')).encode("utf-8"),
            key_serializer=lambda k: k.encode("utf-8") if k else None,
        )
        await self._producer.start()

    async def stop(self) -> None:
        if self._consumer is not None:
            await self._consumer.stop()
            self._consumer = None
        if self._producer is not None:
            await self._producer.stop()
            self._producer = None

    async def produce(self, topic: str, value: dict[str, Any], key: Optional[str] = None) -> None:
        assert self._producer is not None, "Kafka producer not started"
        try:
            await self._producer.send_and_wait(
                topic,
                value,
                key=key,
            )
        except Exception as e:
            logger.error(f"Failed to send message to Kafka: {e}")
            raise

    async def _ensure_consumer_started(self) -> None:
        if self._consumer is not None:
            return
        backoff_seconds = 1.0
        for attempt in range(1, 16):
            try:
                # Standard group consumer
                self._consumer = AIOKafkaConsumer(
                    settings.kafka_topic_outgoing,
                    bootstrap_servers=settings.kafka_bootstrap_servers,
                    group_id=settings.kafka_consumer_group,
                    enable_auto_commit=True,
                    value_deserializer=lambda v: json.loads(v.decode("utf-8")),
                    key_deserializer=lambda k: k.decode("utf-8") if k else None,
                    auto_offset_reset="latest",
                    session_timeout_ms=30000,  # Увеличиваем (было 15000)
                    request_timeout_ms=40000,  # Увеличиваем (было 20000)
                    metadata_max_age_ms=30000,  # Увеличиваем (было 15000)
                )
                await self._consumer.start()
                logger.info(
                    "Kafka group consumer started (group=%s, bootstrap=%s)",
                    settings.kafka_consumer_group,
                    settings.kafka_bootstrap_servers,
                )
                return
            except Exception as e:
                logger.warning("Kafka consumer start failed (attempt %d/15): %s", attempt, e)
                await asyncio.sleep(backoff_seconds)
                backoff_seconds = min(backoff_seconds * 1.5, 5.0)
        raise RuntimeError("Kafka consumer could not be started after multiple retries")

    # Admin client not required; broker auto-creates topics when enabled

    async def consume_outgoing(self) -> AsyncIterator[tuple[str, dict[str, Any]]]:
        await self._ensure_consumer_started()
        assert self._consumer is not None
        try:
            async for msg in self._consumer:
                key = msg.key if msg.key else ""
                yield key, msg.value
        finally:
            pass


kafka_bus = KafkaBus()



