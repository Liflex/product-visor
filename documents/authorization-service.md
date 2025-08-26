## Authorization Service: правила и соглашения

См. также `documents/common-rules.md` для общих принципов. Ниже — специфичные правила для сервиса.

### Назначение
OAuth2 Authorization Server + Resource Server (JWT). Хранение авторизаций в Redis/DB, выдача токенов, управление сессиями.

### Безопасность
- Все запросы защищены через Spring Security Resource Server (JWT Bearer).
- JwtUtil из common-core не используется напрямую здесь; этот сервис является источником JWT.
- Конфиг: `AuthorizationServerConfig`, `OAuth2ServerConfig`, `OAuth2ResourceServerConfig`.
- Хранение OAuth2 авторизаций: Redis (`RedisOAuth2AuthorizationService`) при включенной конфигурации.
- Политика паролей: `PasswordPolicyService` (валидация сложности, частота смены, блокировки).
- Rate limiting: `RateLimitService` для чувствительных эндпоинтов (логин/refresh).

### Доменные сущности
- `User` (id=UUID). Все внешние сервисы обязаны хранить owner_user_id как UUID.

### Контроллеры
- `UserController`: CRUD пользователей (admin-only).
- `SessionController`: управление сессиями/выходом.
- `SimpleTokenController`: технические операции с токенами (dev/admin).

### Конфигурация
- application-common.yml параметры должны наследоваться в окружениях (docker/prod).
- Корс и кэш управляются `CorsConfig`, `CacheConfig`.

### DTO и ответы
- Возвращать минимум данных пользователя, скрывать чувствительные поля.
- Ошибки в формате JSON с кодом/сообщением без стек-трейсов.

### Логирование
- Info: события входа/выхода, выдача/отзыв токенов.
- Error: аномалии авторизации, превышение rate-limit, недействительные токены.

### Миграции
- Flyway `V1__Create_users_table.sql`, `V2__OAuth2_Tables.sql`, последующие – только через миграции.


