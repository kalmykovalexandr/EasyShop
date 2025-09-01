FROM maven:3.9-eclipse-temurin-21 AS build
ARG GITHUB_ACTOR
ARG GITHUB_TOKEN
COPY settings.xml /usr/share/maven/ref/settings.xml
COPY pom.xml ./pom.xml
COPY common-security/ ./common-security/
COPY common-web/ ./common-web/
COPY auth-service/ ./auth-service/
COPY product-service/ ./product-service/
COPY order-service/ ./order-service/
COPY api-gateway/ ./api-gateway/
RUN --mount=type=cache,target=/root/.m2 \
    mvn --settings /usr/share/maven/ref/settings.xml -q -DskipTests \
        dependency:go-offline install && \
    cp -r /root/.m2 /m2
ENV MAVEN_OPTS="-Dmaven.repo.local=/m2"
