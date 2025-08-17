from __future__ import annotations

import json
from typing import Any

import redis


class SessionStore:
    def __init__(self, url: str = "redis://localhost:6379/0", ttl: int = 1800) -> None:
        self._client = redis.Redis.from_url(url, decode_responses=True)
        self._ttl = ttl

    def _key(self, chat_id: int) -> str:
        return f"session:{chat_id}"

    def get(self, chat_id: int) -> dict[str, Any]:
        raw = self._client.get(self._key(chat_id))
        if not raw:
            return {}
        try:
            return json.loads(raw)
        except Exception:
            return {}

    def set(self, chat_id: int, data: dict[str, Any]) -> None:
        self._client.set(self._key(chat_id), json.dumps(data, ensure_ascii=False), ex=self._ttl)

    def clear(self, chat_id: int) -> None:
        self._client.delete(self._key(chat_id))


session_store = SessionStore()



