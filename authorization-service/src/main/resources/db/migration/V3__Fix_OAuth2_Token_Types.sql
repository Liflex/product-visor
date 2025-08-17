-- Исправление типов данных для токенов OAuth2
-- Spring Authorization Server 1.2.3 ожидает bytea для токенов

-- Удаляем старую таблицу
DROP TABLE IF EXISTS oauth2_authorization;

-- Создаем таблицу с правильными типами данных
CREATE TABLE oauth2_authorization (
    id varchar(100) primary key,
    registered_client_id varchar(100) not null,
    principal_name varchar(200) not null,
    authorization_grant_type varchar(100) not null,
    authorized_scopes varchar(1000),
    attributes text,
    state varchar(500),
    authorization_code_value text,
    authorization_code_issued_at timestamp,
    authorization_code_expires_at timestamp,
    authorization_code_metadata text,
    access_token_value text,
    access_token_issued_at timestamp,
    access_token_expires_at timestamp,
    access_token_metadata text,
    access_token_type varchar(100),
    access_token_scopes varchar(1000),
    oidc_id_token_value text,
    oidc_id_token_issued_at timestamp,
    oidc_id_token_expires_at timestamp,
    oidc_id_token_metadata text,
    refresh_token_value text,
    refresh_token_issued_at timestamp,
    refresh_token_expires_at timestamp,
    refresh_token_metadata text,
    user_code_value text,
    user_code_issued_at timestamp,
    user_code_expires_at timestamp,
    user_code_metadata text,
    device_code_value text,
    device_code_issued_at timestamp,
    device_code_expires_at timestamp,
    device_code_metadata text
);

-- Создаем индексы
CREATE INDEX IF NOT EXISTS oauth2_authorization_principal_name_idx ON oauth2_authorization (principal_name);
CREATE INDEX IF NOT EXISTS oauth2_authorization_registered_client_id_idx ON oauth2_authorization (registered_client_id);
