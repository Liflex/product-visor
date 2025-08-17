FROM python:3.11-slim

WORKDIR /app
COPY pyproject.toml /app/
RUN pip install --no-cache-dir poetry && poetry config virtualenvs.create false && poetry install --no-root

COPY bot /app/bot

RUN apt-get update && apt-get install -y --no-install-recommends socat && rm -rf /var/lib/apt/lists/*

ENV PYTHONUNBUFFERED=1
CMD ["sh", "-c", "socat TCP-LISTEN:9092,fork,reuseaddr,bind=127.0.0.1 TCP:kafka:9092 & socat TCP6-LISTEN:9092,fork,reuseaddr,bind=::1 TCP:kafka:9092 & python -m bot"]



