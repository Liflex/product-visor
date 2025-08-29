# Анализ фронтенд кодовой базы: Product Visor

## 📁 Структура проекта

- `src/`
  - `components/` — UI-компоненты, страницы и составные виджеты (Orders, Products, Credentials, Management и др.). Файл `App.jsx` связывает роутинг и лэйаут.
  - `services/` — слой доступа к API (axios-клиенты через `utils/http-client.js`, генерация URL в `config/api-config.js`). Каждый домен (orders, products, profile и т.д.) имеет отдельный сервис.
  - `contexts/` — контексты приложения. `AuthContext.jsx` инкапсулирует аутентификацию, хранение пользователя и выбранной компании.
  - `hooks/` — кастомные хуки для бизнес-логики (продукты, категории, валидация форм, сканер штрихкодов).
  - `utils/` — утилиты (HTTP-клиент на axios с перехватчиками, auth-utils и пр.).
  - `config/` — конфигурация API и эндпоинтов для микросервисов.
  - `assets/`, `App.css`, `index.css` — статические ресурсы и стили.
- `index.html` — точка входа Vite, монтирует `src/main.jsx`.
- `documents/` — документация проекта; сюда сохранен этот анализ в `project.md`.

Принцип организации кода: смешанный layer/domain-based — UI (`components`), инфраструктура (`utils`, `config`), доменные сервисы (`services`), состояние (`contexts`, `hooks`). Навигация централизована в `App.jsx`.

Пример дерева директорий (до 3 уровней):

```
src/
  components/
    Orders.jsx
    OrdersTable.jsx
    ProductAll.jsx
    ProductFormNew.jsx
    ProtectedRoute.jsx
    DropdownMenu.jsx
    ...
  services/
    orderService.js
    productService.js
    companyService.js
    profileService.js
    ...
  contexts/
    AuthContext.jsx
  hooks/
    use-products.js
    use-categories.js
    use-form-validation.js
  utils/
    http-client.js
    auth-utils.js
  config/
    api-config.js
```

## 🛠 Технологический стек

- **Фреймворк**: React 18 (JSX, функциональные компоненты, хуки)
- **Сборка**: Vite (см. `index.html` с `type="module"` и `/src/main.jsx`)
- **Маршрутизация**: `react-router-dom` (`BrowserRouter`, `Routes`, `Route`, `Navigate`)
- **HTTP**: axios (централизованный клиент с перехватчиками)
- **Стили**: Tailwind-like utility классы в JSX (предположительно TailwindCSS; явный конфиг не показан, но классы соответствуют)
- **Управление состоянием**: React Context (`AuthContext.jsx`) + локальный state и кастомные хуки
- **Интеграции**: микросервисы (`product-visor-backend`, `order-service`, `ozon-service`, `yandex-service`, `client`, `auth`) через `config/api-config.js`

Версии пакетов в коде напрямую не указаны (package.json не предоставлен). Сеть и URL указывают на локальную инфраструктуру.

## 🏗 Архитектура

- **Компонентная архитектура**: Страницы/виджеты в `components`, переиспользуемые UI блоки (`OrdersTable`, `DropdownMenu`, модалки). Вертикальные фичи разделены файлами, без глубокой папочной структуры по фичам.
- **Разделение логики**:
  - UI в компонентах
  - Бизнес-логика и эффекты в `hooks/*` (например, `use-products.js`)
  - Доступ к данным в `services/*`, построение URL — `config/api-config.js`
  - Транспарентные перехватчики запросов/ответов — `utils/http-client.js`
- **Состояние**:
  - Глобально: `AuthContext` (аутентификация, профиль, выбранная компания)
  - Локально: `useState`/`useEffect` в компонентах
  - Кастомные хуки: инкапсулируют CRUD и фильтрацию (см. `use-products.js`)
- **API-слой**:
  - Два axios-инстанса: `httpClient` (c baseURL и версией) и `microservicesHttpClient` (без baseURL, под прокси Vite)
  - Генерация URL через `API_URLS`/`buildApiUrl` с учетом сервиса
  - Перехватчики: добавление `Authorization` и `X-Company-Id`, обработка 401 с редиректом на `/login`
- **Роутинг**:
  - `App.jsx` объявляет публичные/защищенные маршруты
  - `ProtectedRoute.jsx` блокирует доступ до завершения проверки и редиректит неавторизованных
- **Ошибки и загрузка**:
  - `ErrorBoundary` в `App.jsx` + компонент `ErrorMessage`
  - Спиннеры при загрузке (в `ProtectedRoute`, `AddProductPage` и др.)

## 🎨 UI/UX и стилизация

- Utility-first классы (Tailwind-подобные) для верстки, состояния, состояний hover/disabled
- Навигация с выпадающими меню (`DropdownMenu`) и индикацией активного роута
- Табличные представления с пагинацией (`OrdersTable`), модалки (`CancelReasonModal`)

## ✅ Качество кода

- **Именование**: ясные имена функций/переменных, доменные сервисы названы по сущности
- **Типизация**: JavaScript без TS; типы описываются JSDoc и сигнатурами; отсутствие статической типизации — потенциальная зона улучшения
- **Тесты**: в фронтенде не обнаружены; можно добавить unit/RTK Query/React Testing Library
- **Документация**: краткие JSDoc-комментарии в ключевых файлах (`App.jsx`, `http-client.js`, хуки)

## 🔧 Ключевые компоненты

1) `AuthContext.jsx` — аутентификация и выбор компании
- Роль: хранит `isAuthenticated`, `user`, `companyId`, логин/логаут, загрузка профиля и текущей компании
- Пример:
```jsx
// Инициализация токена и профиля
useEffect(() => {
  const token = localStorage.getItem('authToken');
  if (token) {
    setAuthToken(token);
    setIsAuthenticated(true);
    const profile = await profileService.getProfile();
    setUser(profile);
  }
  setIsLoading(false);
}, []);
```
- Интеграции: `profileService`, `companyService`, `API_URLS.AUTH`, `utils/http-client`

2) `ProtectedRoute.jsx` — защита маршрутов
- Роль: отображает лоадер во время проверки и редиректит на `/login` при отсутствии авторизации
- Пример:
```jsx
if (isLoading) return <div className="min-h-screen flex items-center justify-center bg-gray-900">...</div>;
if (!isAuthenticated) return <Navigate to="/login" state={{ from: location }} replace />;
return children;
```

3) `utils/http-client.js` — единая конфигурация HTTP
- Роль: настраивает axios, добавляет заголовки, обрабатывает ошибки/401
- Пример:
```js
const requestInterceptor = (config) => {
  if (__auth.token) config.headers.Authorization = `Bearer ${__auth.token}`;
  if (__auth.companyId) config.headers['X-Company-Id'] = __auth.companyId;
  return config;
};
httpClient.interceptors.request.use(requestInterceptor);
httpClient.interceptors.response.use(r => r, responseErrorInterceptor);
```

4) `use-products.js` — бизнес-логика продуктов
- Роль: загрузка, поиск/фильтрация, CRUD, ошибки/лоадинг
- Пример:
```js
const loadProducts = useCallback(async () => {
  setIsLoading(true);
  try {
    const data = await getProducts();
    setProducts(data);
    setFilteredProducts(data);
  } catch (err) {
    setError(err.message);
  } finally {
    setIsLoading(false);
  }
}, []);
```

5) `OrdersTable.jsx` — таблица заказов с пагинацией и модалкой
- Роль: отображение заказов, статусов и страницы, просмотр причины отмены
- Пример:
```jsx
{orders.map(order => (
  <tr key={order.postingNumber || order.id}>
    <td>{order.postingNumber || order.orderBarcode || '-'}</td>
    <td>
      <span className={`px-2 py-1 text-xs rounded-full ${getStatusColor(order.status)}`}>
        {order.status}
      </span>
    </td>
  </tr>
))}
```

## 📋 Выводы и рекомендации

- **Сильные стороны**:
  - Чистое разделение UI/бизнес-логики/данных; понятные сервисы и хуки
  - Централизованный HTTP-клиент с единообразной обработкой ошибок и заголовков
  - Четкая маршрутизация и защита роутов, ErrorBoundary
  - Модульная структура с хорошим переиспользованием компонентов
- **Зоны улучшения**:
  - Добавить TypeScript для статической типизации и контрактов сервисов
  - Ввести состояние запроса на уровне хуков/сервисов с унифицированными типами ответов
  - Включить тесты (unit/e2e), мерить покрытие критичных модулей
  - Рассмотреть React Query/RTK Query для кэширования и инвалидации данных
  - Явно задокументировать Tailwind конфигурацию и дизайн-токены
- **Уровень сложности**: middle-friendly (чёткая архитектура, но требуется контекст по микросервисам и авторизации)

