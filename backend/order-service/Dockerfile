FROM easyshop-build-base:latest AS build
ARG GITHUB_ACTOR
ARG GITHUB_TOKEN
WORKDIR /app
COPY order-service/pom.xml ./order-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn --settings /usr/share/maven/ref/settings.xml -q -DskipTests -f order-service/pom.xml dependency:go-offline
COPY order-service/src ./order-service/src
RUN --mount=type=cache,target=/root/.m2 mvn --settings /usr/share/maven/ref/settings.xml -q -DskipTests -f order-service/pom.xml package
FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /app/order-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]