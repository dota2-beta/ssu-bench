# SsuBench

REST API платформа. Сервис, где заказчики размещают задания, исполнители откликаются на них, а оплата выполненной работы производится виртуальными баллами.

## Стек

- Java 17
- Spring Boot 4
- Spring Data JPA
- PostgreSQL
- Liquibase
- Docker Compose
- JWT
- BCrypt

## Запуск с Docker

Запуск всей инфраструктуры (база + приложение):

```bash
docker-compose up --build
```

Остановка:

```bash
docker-compose down
```

Остановка с удалением томов:

```bash
docker-compose down -v
```

Сервис будет доступен на `http://localhost:8080`.

## Миграции
Миграции Liquibase применятся автоматически при старте приложения.

Скрипты миграций находятся в папке src/main/resources/db/changelog/migrations

## Переменные окружения

Все переменные настраиваются в файле `.env`.

- `SERVER_PORT` — порт приложения
- `JWT_SECRET` — секретный ключ для подписи JWT
- `JWT_EXPIRES_IN` — время жизни токена
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` — параметры подключения к Postgres
- `HTTP_KEEP_ALIVE_TIMEOUT`, `HTTP_REQUEST_TIMEOUT`, `SHUTDOWN_TIMEOUT` — таймауты сервера

## Прочие команды

Запуск тестов:

```bash
cd backend
.\mvnw test
```

## Примеры curl

### Подготовка переменных

```bash
export BASE_URL=http://localhost:8080/api
export MSYS_NO_PATHCONV=1
export CUSTOMER_NAME=ivan
export EXECUTOR_NAME=petr
export ADMIN_NAME=admin
export PASSWORD=password123
export TASK_COST=50
```

### Регистрация пользователей

```bash
# Регистрация заказчика
curl -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$CUSTOMER_NAME\",\"password\":\"$PASSWORD\",\"role\":\"CUSTOMER\"}"

# Регистрация исполнителя
curl -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$EXECUTOR_NAME\",\"password\":\"$PASSWORD\",\"role\":\"EXECUTOR\"}"
```

### Получение токенов (Логин)

```bash
# Токен заказчика
export CUSTOMER_TOKEN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$CUSTOMER_NAME\",\"password\":\"$PASSWORD\"}" | \
  sed 's/.*"token":"//;s/".*//')

# Токен исполнителя
export EXECUTOR_TOKEN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$EXECUTOR_NAME\",\"password\":\"$PASSWORD\"}" | \
  sed 's/.*"token":"//;s/".*//')

# Токен администратора (дефолтный админ, логин пароль из миграции)
export ADMIN_TOKEN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}" | \
  sed 's/.*"token":"//;s/".*//')
```

1. **Создание задачи (Заказчик):**
```bash
export TASK_ID=$(curl -s -X POST "$BASE_URL/tasks" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"Task 1\",\"description\":\"Java API\",\"cost\":$TASK_COST}" | \
  sed 's/.*"id"://;s/[,}].*//')
```

2. **Отклик на задачу (Исполнитель):**
```bash
export BID_ID=$(curl -s -X POST "$BASE_URL/tasks/$TASK_ID/bids" \
  -H "Authorization: Bearer $EXECUTOR_TOKEN" | \
  sed 's/.*"bidId"://;s/[,}].*//')
```

3. **Выбор исполнителя (Заказчик):**
```bash
curl -X POST "$BASE_URL/tasks/$TASK_ID/select-executor/$BID_ID" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

4. **Отметка о выполнении (Исполнитель):**
```bash
curl -X POST "$BASE_URL/tasks/$TASK_ID/complete" \
  -H "Authorization: Bearer $EXECUTOR_TOKEN"
```

5. **Подтверждение выполнения и оплата (Заказчик):**
```bash
curl -X POST "$BASE_URL/tasks/$TASK_ID/confirm" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

### Проверка результата и профиля

```bash
# Проверка баланса и профиля заказчика
echo "Профиль заказчика:"
curl -s -X GET "$BASE_URL/users/me" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"

# Проверка баланса исполнителя
echo -e "\nПрофиль исполнителя:"
curl -s -X GET "$BASE_URL/users/me" \
  -H "Authorization: Bearer $EXECUTOR_TOKEN"

# Блокировка пользователя (Администратор)
curl -X POST "$BASE_URL/admin/users/block" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d "{\"username\":\"$EXECUTOR_NAME\",\"blocked\":true}"
```

## OpenAPI

Описание API находится в файле `openapi.yaml`.

Развернуть визуализацию локально:
```bash
docker run -p 8081:8080 \
  -v "$(pwd)/openapi.yaml:/var/specs/openapi.yaml" \
  -e SWAGGER_JSON=/var/specs/openapi.yaml \
  swaggerapi/swagger-ui
```
После чего swagger будет доступен на `http://localhost:8081`.
