# EasyShop (monorepo)

## Stack
- Java 21, Spring Boot (auth/product/order + api-gateway)
- React + Vite
- Postgres
- Docker Compose (dev/prod)

## Quickly start (dev)
```bash
DOCKER_BUILDKIT=1 docker compose -f infra/dev/docker-compose.dev.yml up --build
```

> Docker builds require BuildKit. Ensure `DOCKER_BUILDKIT=1` is set when building images.

## Maven repository

Backend modules resolve internal artifacts from the Maven repository URL
specified by `MAVEN_REPO_URL` in your `.env`. To point the build to a new
repository, update this variable.

## Environment variables

Backend services read configuration from environment variables. Provide them via
`.env` files for local runs or CI secrets in automated pipelines:

- `SERVER_PORT` – port the service listens on.
- `SPRING_DATASOURCE_URL` – JDBC connection string for Postgres.
- `SPRING_DATASOURCE_USERNAME` – database username.
- `SPRING_DATASOURCE_PASSWORD` – database password (secret).
- `JWT_SECRET` – signing key for JWT tokens (secret).
- `JWT_TTL_MINUTES` – token lifetime in minutes (defaults to `60`).
- `AUTH_URL` – base URL of the auth service for the API gateway.
- `PRODUCT_URL` – base URL of the product service for the API gateway.
- `ORDER_URL` – base URL of the order service for the API gateway.
- `PRODUCT_BASE_URL` – product service URL used by the order service.

## Production setup

On the server, copy the example environment file and fill in the values:

```bash
cp infra/prod/.env.example infra/prod/.env
# edit infra/prod/.env with real values
```

## Сборка и запуск

Перед сборкой установите переменные окружения `GITHUB_ACTOR` и `GITHUB_TOKEN`.

```bash
DOCKER_BUILDKIT=1 docker compose --env-file infra/prod/.env -f infra/prod/docker-compose.prod.yml build
```

## Сборка микросервисов отдельно

Для сборки конкретного сервиса используйте Maven, указывая его модуль. Например, для `auth-service`:

```bash
mvn -f backend/pom.xml -pl auth-service -am package
DOCKER_BUILDKIT=1 docker build -f backend/auth-service/Dockerfile -t auth-service backend
docker run --rm -p 8080:8080 auth-service
```
