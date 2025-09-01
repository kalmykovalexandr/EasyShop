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
