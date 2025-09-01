FROM maven:3.9-eclipse-temurin-21 AS build

LABEL org.opencontainers.image.version="1.0.0"

COPY pom.xml ./pom.xml
COPY common-security/ ./common-security/

RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests -pl common-security install
