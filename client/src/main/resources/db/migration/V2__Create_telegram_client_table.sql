-- Создание таблицы telegram_client в схеме client
CREATE TABLE client.telegram_client (
    chat_id BIGINT PRIMARY KEY,
    bot_id VARCHAR(100),
    username VARCHAR(100),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    registered_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    premium BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_bot_chat UNIQUE (bot_id, chat_id)
);

-- Создание индексов
CREATE INDEX idx_telegram_client_bot_id ON client.telegram_client(bot_id);
CREATE INDEX idx_telegram_client_premium ON client.telegram_client(premium);
CREATE INDEX idx_telegram_client_email ON client.telegram_client(email);

-- Создание триггера для автоматического обновления updated_at
CREATE TRIGGER update_telegram_client_updated_at 
    BEFORE UPDATE ON client.telegram_client 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
