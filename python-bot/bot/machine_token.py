from __future__ import annotations

import time
import base64
import httpx
from .config import settings

_cached_token: str | None = None
_cached_exp: float = 0.0


def get_machine_token() -> str:
    global _cached_token, _cached_exp
    now = time.time()
    if _cached_token and now < _cached_exp - 30:
        return _cached_token
    data = {
        "grant_type": "client_credentials",
        "scope": "internal",
    }
    token_uri = "http://localhost:9099/oauth2/token"
    basic = base64.b64encode(f"svc_product_visor:secret".encode()).decode()
    headers = {
        "Content-Type": "application/x-www-form-urlencoded",
        "Authorization": f"Basic {basic}",
    }
    with httpx.Client(timeout=5.0) as client:
        r = client.post(token_uri, data=data, headers=headers)
        if r.status_code == 401:
            data_fallback = {
                "grant_type": "client_credentials",
                "scope": "internal",
                "client_id": "svc_product_visor",
                "client_secret": "secret",
            }
            r = client.post(token_uri, data=data_fallback, headers={"Content-Type": "application/x-www-form-urlencoded"})
        r.raise_for_status()
        j = r.json()
        _cached_token = j.get("access_token")
        expires_in = int(j.get("expires_in", 300))
        _cached_exp = now + expires_in
        return _cached_token or ""



