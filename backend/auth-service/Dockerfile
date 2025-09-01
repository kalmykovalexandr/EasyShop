FROM maven:3.9-eclipse-temurin-21 AS build
ARG GITHUB_ACTOR
ARG GITHUB_TOKEN
WORKDIR /app
COPY settings.xml /usr/share/maven/ref/settings.xml
COPY pom.xml ./
COPY common-security ./common-security
COPY common-web ./common-web
COPY auth-service ./auth-service
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests -pl auth-service -am package
FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /app/auth-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]
