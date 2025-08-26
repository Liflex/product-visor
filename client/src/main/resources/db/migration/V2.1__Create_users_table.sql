-- Создание таблицы пользователей в схеме client
CREATE TABLE client.user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    photo VARCHAR(500),
    avatar bytea,
    role VARCHAR(50) NOT NULL DEFAULT 'PRIVATE_PERSON',
    is_verified BOOLEAN NOT NULL DEFAULT false,
    registration_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    locale VARCHAR(10) NOT NULL DEFAULT 'ru_RU',
    timezone VARCHAR(50) NOT NULL DEFAULT 'Europe/Moscow',
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    birth_date DATE,
    phone VARCHAR(20),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Создание индексов
CREATE INDEX idx_users_email ON client.user(email);
CREATE INDEX idx_users_role ON client.user(role);
CREATE INDEX idx_users_verified ON client.user(is_verified);

-- Создание триггера для автоматического обновления updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON client.user
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
