# Hibernate Auto Generation Configuration

## Проблема
Hibernate `ddl-auto: update` не всегда корректно добавляет новые столбцы в существующие таблицы.

## Решение

### Вариант 1: Create-Drop (Рекомендуется для разработки)

Используйте `create-drop` для полной пересоздания схемы:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        hbm2ddl:
          auto: create-drop
```

**Преимущества:**
- ✅ Автоматически создает все столбцы
- ✅ Гарантированно работает
- ✅ Простота настройки

**Недостатки:**
- ❌ Удаляет все данные при перезапуске
- ❌ Только для разработки

### Вариант 2: Update с ручной миграцией

1. **Временно переключитесь на `create-drop`:**
   ```yaml
   ddl-auto: create-drop
   ```

2. **Запустите приложение один раз** - это создаст все столбцы

3. **Переключитесь обратно на `update`:**
   ```yaml
   ddl-auto: update
   ```

4. **Используйте production конфигурацию:**
   ```bash
   java -jar app.jar --spring.profiles.active=prod
   ```

### Вариант 3: Ручное выполнение SQL

Если автогенерация не работает, выполните SQL вручную:

```sql
-- Добавить столбец quantity
ALTER TABLE visor.product ADD COLUMN IF NOT EXISTS quantity INTEGER NOT NULL DEFAULT 0;

-- Добавить столбец image
ALTER TABLE visor.product ADD COLUMN IF NOT EXISTS image BYTEA;

-- Добавить комментарии
COMMENT ON COLUMN visor.product.quantity IS 'Product quantity in stock';
COMMENT ON COLUMN visor.product.image IS 'Product image stored as byte array in database';
```

## Текущая конфигурация

### Development (application.yml)
```yaml
ddl-auto: create-drop  # Пересоздает схему при каждом запуске
```

### Production (application-prod.yml)
```yaml
ddl-auto: update       # Обновляет схему без удаления данных
```

## Запуск с разными профилями

```bash
# Development (с пересозданием схемы)
java -jar app.jar

# Production (с сохранением данных)
java -jar app.jar --spring.profiles.active=prod
```

## Проверка схемы

После запуска проверьте, что столбцы созданы:

```sql
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns 
WHERE table_schema = 'visor' 
AND table_name = 'product'
ORDER BY ordinal_position;
```

Ожидаемый результат:
```
column_name           | data_type | is_nullable | column_default
---------------------+-----------+-------------+----------------
id                   | bigint    | NO          | nextval('...')
name                 | character | NO          | 
image                | bytea     | YES         | 
image_url            | character | YES         | 
barcode              | character | YES         | 
quantity             | integer   | NO          | 0
category_id          | bigint    | YES         | 
``` 