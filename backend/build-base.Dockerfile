FROM maven:3.9-eclipse-temurin-21

WORKDIR /app

# Copy project root POM and common-security module
COPY pom.xml ./pom.xml
COPY common-security/pom.xml ./common-security/pom.xml
COPY common-security/src ./common-security/src

# Pre-build common-security with cached dependencies
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -N -DskipTests install && \
    mvn -q -DskipTests -pl common-security install

