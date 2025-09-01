FROM easyshop-build-base:latest AS build
ARG GITHUB_ACTOR
ARG GITHUB_TOKEN
WORKDIR /app
COPY api-gateway/pom.xml ./api-gateway/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn --settings /usr/share/maven/ref/settings.xml -q -DskipTests -f api-gateway/pom.xml dependency:go-offline
COPY api-gateway/src ./api-gateway/src
RUN --mount=type=cache,target=/root/.m2 mvn --settings /usr/share/maven/ref/settings.xml -q -DskipTests -f api-gateway/pom.xml package
FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /app/api-gateway/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]
