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

## Build image versioning

The shared build image `easyshop-build-base` is tagged with a semantic
version. Build and tag the image explicitly, for example:

```
docker build -t easyshop-build-base:1.0.0 -f backend/build-base.Dockerfile backend
```

Service Dockerfiles pin to a specific version tag. When the base image is
updated, create a new tag (e.g., `1.0.1`, `1.1.0`, etc.) and update the
service Dockerfiles accordingly instead of reusing `latest`.
