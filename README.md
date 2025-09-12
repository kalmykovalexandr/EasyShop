# EasyShop (monorepo)

## Stack
- Java 21, Spring Boot (auth/product/purchase + api-gateway)
- React + Vite
- Postgres
- Docker Compose (dev/prod)

## Quickly start (dev)
```bash
DOCKER_BUILDKIT=1 docker compose -f infra/docker-compose.yml up --build
```

Run with a specific Spring profile, for example `local`:

```bash
SPRING_PROFILES_ACTIVE=local DOCKER_BUILDKIT=1 docker compose -f infra/docker-compose.yml up --build
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
- `PURCHASE_URL` – base URL of the purchase service for the API gateway.
- `PRODUCT_BASE_URL` – product service URL used by the purchase service.

### Local environment setup

```bash
cp infra/.env.example .env
# edit .env with local values
```

## Production setup

On the server, copy the example environment file and fill in the values:

```bash
cp infra/prod/.env.example infra/prod/.env
# edit infra/prod/.env with real values
```

## Build and Run

Before building, set environment variables `GITHUB_ACTOR` and `GITHUB_TOKEN`.

```bash
DOCKER_BUILDKIT=1 docker compose --env-file infra/prod/.env -f infra/prod/docker-compose.prod.yml build
```

## Build Microservices Separately

To build a specific service, use Maven specifying its module. For example, for `auth-service`:

```bash
mvn -f backend/pom.xml -pl auth-service -am package
DOCKER_BUILDKIT=1 docker build -f backend/auth-service/Dockerfile -t auth-service backend
docker run --rm -p 8080:8080 auth-service
```
