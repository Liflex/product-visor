# Скрипт для проверки OAuth2 клиента в базе данных

param(
    [string]$DbHost = "localhost",
    [int]$Port = 5433,
    [string]$Database = "product_visor",
    [string]$Username = "postgres",
    [string]$Password = "postgres"
)

Write-Host "🔍 Проверка OAuth2 клиента в базе данных..." -ForegroundColor Yellow

# SQL запрос для проверки клиента
$sql = @"
SELECT 
    id,
    client_id,
    client_name,
    client_authentication_methods,
    authorization_grant_types,
    scopes,
    client_settings,
    token_settings
FROM oauth2_registered_client 
WHERE client_id = 'oficiant-client';
"@

# Выполняем запрос
try {
    $result = psql -h $Host -p $Port -d $Database -U $Username -c $sql 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Клиент найден:" -ForegroundColor Green
        Write-Host $result
    } else {
        Write-Host "❌ Ошибка при выполнении запроса:" -ForegroundColor Red
        Write-Host $result
    }
} catch {
    Write-Host "❌ Ошибка подключения к базе данных: $_" -ForegroundColor Red
}

Write-Host "`n🔍 Проверка таблицы авторизаций..." -ForegroundColor Yellow

# Проверяем структуру таблицы авторизаций
$sql2 = @"
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'oauth2_authorization' 
AND table_schema = 'authorization'
ORDER BY ordinal_position;
"@

try {
    $result2 = psql -h $Host -p $Port -d $Database -U $Username -c $sql2 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Структура таблицы oauth2_authorization:" -ForegroundColor Green
        Write-Host $result2
    } else {
        Write-Host "❌ Ошибка при проверке структуры таблицы:" -ForegroundColor Red
        Write-Host $result2
    }
} catch {
    Write-Host "❌ Ошибка подключения к базе данных: $_" -ForegroundColor Red
}
