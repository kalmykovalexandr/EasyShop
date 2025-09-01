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

## Production setup

On the server, copy the example environment file and fill in the values:

```bash
cp infra/prod/.env.example infra/prod/.env
# edit infra/prod/.env with real values
```

## Сборка и запуск

Перед сборкой установите переменные окружения `GITHUB_ACTOR` и `GITHUB_TOKEN`.

```bash
DOCKER_BUILDKIT=1 docker compose --env-file infra/prod/.env.prod -f infra/prod/docker-compose.prod.yml build
```

## Сборка микросервисов отдельно

Для сборки конкретного сервиса используйте Maven, указывая его модуль. Например, для `auth-service`:

```bash
mvn -f backend/pom.xml -pl auth-service -am package
DOCKER_BUILDKIT=1 docker build -f backend/auth-service/Dockerfile -t auth-service backend
docker run --rm -p 8080:8080 auth-service
```
