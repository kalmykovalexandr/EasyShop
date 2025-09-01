FROM maven:3.9-eclipse-temurin-21 AS build

COPY pom.xml ./pom.xml
COPY common-security/ ./common-security/
COPY common-web/ ./common-web/

# Install shared modules with Maven, using a cached local repository
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests -pl common-security,common-web install
