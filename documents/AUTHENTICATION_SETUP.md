# Настройка системы авторизации

## Обзор

Система авторизации была настроена для автоматического перенаправления на страницу входа при получении 401 ошибок от сервера.

## Компоненты системы

### 1. AuthContext (`src/contexts/AuthContext.jsx`)
- Управляет состоянием аутентификации
- Предоставляет функции `login`, `logout`, `setCompany`
- Автоматически проверяет токен при загрузке приложения
- Перенаправляет на главную страницу после успешного входа

### 2. ProtectedRoute (`src/components/ProtectedRoute.jsx`)
- Компонент-обертка для защиты маршрутов
- Проверяет аутентификацию перед отображением контента
- Перенаправляет на `/login` если пользователь не аутентифицирован
- Показывает индикатор загрузки во время проверки

### 3. HTTP Client (`src/utils/http-client.js`)
- Автоматически обрабатывает 401 ошибки
- Очищает токен и данные компании при получении 401
- Перенаправляет на страницу входа
- Добавляет токен авторизации ко всем запросам

### 4. Auth Utils (`src/utils/auth-utils.js`)
- Утилиты для работы с авторизацией
- Функции для проверки, получения и очистки токенов
- Функция перенаправления на страницу входа

## Как это работает

1. **При загрузке приложения:**
   - AuthContext проверяет наличие токена в localStorage
   - Если токен есть, устанавливает состояние `isAuthenticated = true`
   - Если токена нет, оставляет `isAuthenticated = false`

2. **При попытке доступа к защищенному маршруту:**
   - ProtectedRoute проверяет `isAuthenticated`
   - Если `false`, перенаправляет на `/login`
   - Если `true`, отображает защищенный контент

3. **При получении 401 ошибки:**
   - HTTP клиент автоматически очищает токен
   - Перенаправляет пользователя на страницу входа
   - Показывает сообщение об ошибке

4. **При успешном входе:**
   - Токен сохраняется в localStorage
   - Состояние `isAuthenticated` устанавливается в `true`
   - Пользователь перенаправляется на главную страницу

## Использование

### Защита маршрутов
```jsx
<Route path="/protected" element={
  <ProtectedRoute>
    <ProtectedComponent />
  </ProtectedRoute>
} />
```

### Использование контекста авторизации
```jsx
import { useAuth } from '../contexts/AuthContext.jsx';

const MyComponent = () => {
  const { isAuthenticated, user, logout } = useAuth();
  
  if (!isAuthenticated) {
    return <div>Please log in</div>;
  }
  
  return (
    <div>
      Welcome, {user?.username}!
      <button onClick={logout}>Logout</button>
    </div>
  );
};
```

### Настройка компании
```jsx
const { setCompany } = useAuth();
setCompany('company-id');
```

## Конфигурация

### Токены
- Токен авторизации: `localStorage.getItem('authToken')`
- ID компании: `localStorage.getItem('companyId')`

### API Endpoints
- Вход: `POST http://192.168.1.59:9099/api/auth/login`
- Формат данных: `application/x-www-form-urlencoded`
- Поля: `username`, `password`
- `client_id` и `client_secret` берутся из конфигурации сервера

## Безопасность

- Токены автоматически очищаются при 401 ошибках
- Все защищенные маршруты проверяют аутентификацию
- Токен добавляется ко всем HTTP запросам автоматически
- При выходе все данные авторизации очищаются

## Отладка

Для отладки авторизации можно использовать:

1. **Проверка токена в браузере:**
   ```javascript
   localStorage.getItem('authToken')
   ```

2. **Очистка данных авторизации:**
   ```javascript
   localStorage.removeItem('authToken')
   localStorage.removeItem('companyId')
   ```

3. **Проверка состояния в контексте:**
   ```javascript
   const { isAuthenticated, user } = useAuth();
   console.log('Auth state:', { isAuthenticated, user });
   ```

4. **Тестирование API авторизации:**
   ```javascript
   // В консоли браузера
   testAuth().then(result => console.log(result));
   ```

## Отладка проблем

### Проблемы с CORS
Если возникают проблемы с CORS, убедитесь что authorization-service настроен правильно.

### Проверка доступности сервиса

#### Curl команда для Postman:
```bash
curl -X POST http://192.168.1.59:9099/api/auth/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin&password=password"
```

#### Настройка в Postman:
1. **Method**: POST
2. **URL**: `http://192.168.1.59:9099/api/auth/login`
3. **Headers**:
   - `Content-Type`: `application/x-www-form-urlencoded`
4. **Body** (x-www-form-urlencoded):
   - `username`: `admin`
   - `password`: `password`

#### Ожидаемый ответ при успехе:
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 18000,
  "user": "admin"
}
```

#### Возможные ошибки:
- `401 Unauthorized`: Неверные учетные данные
- `400 Bad Request`: Ошибка валидации
- `429 Too Many Requests`: Превышен лимит запросов

### Отладка параметров запроса
В консоли браузера вы увидите логи с параметрами запроса:
```javascript
// В консоли браузера
testAuth().then(result => console.log(result));
```

### Логи сервера
Проверьте логи authorization-service для диагностики проблем аутентификации.

#### Что искать в логах:
1. **Начало запроса**:
   ```
   === LOGIN REQUEST START ===
   Username: admin
   Request received at: 2025-01-23T10:30:00
   Using client_id: oficiant-client
   ```

2. **Аутентификация пользователя**:
   ```
   Authentication successful for user: admin
   ```

3. **Проверка клиента**:
   ```
   Looking up client with ID: oficiant-client
   Client found: oficiant-client
   Client secret validation successful
   ```

4. **Создание токена**:
   ```
   Creating JWT token for user: admin
   User found in database - ID: 1, Email: admin@example.com
   JWT claims created - Subject: admin, Client: oficiant-client, User ID: 1
   JWT token generated successfully
   ```

5. **Успешное завершение**:
   ```
   === LOGIN REQUEST SUCCESSFUL ===
   Response prepared for user: admin
   Token expires in: 18000 seconds
   ```

6. **Ошибки**:
   ```
   === LOGIN REQUEST FAILED ===
   Error during authentication for user: admin
   Exception type: BadCredentialsException
   Exception message: Bad credentials
   ```

#### Команда для просмотра логов:
```bash
# В контейнере Docker
docker logs authorization-service

# Или в файле логов
tail -f logs/authorization-service.log
```
