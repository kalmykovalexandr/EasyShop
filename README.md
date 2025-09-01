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

## Build image versioning

The shared build image `easyshop-build-base` is tagged with a semantic
version. Build and tag the image explicitly, for example:

```
docker build -t easyshop-build-base:1.0.0 -f backend/build-base.Dockerfile backend
```

Service Dockerfiles and CI workflows pin to a specific version tag. When
the base image is updated, create a new tag (e.g., `1.0.1`, `1.1.0`, etc.)
and update all references accordingly instead of reusing `latest`.
