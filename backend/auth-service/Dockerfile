FROM easyshop-build-base:1.0.0 AS build
ARG GITHUB_ACTOR
ARG GITHUB_TOKEN
WORKDIR /app
COPY auth-service/pom.xml ./auth-service/pom.xml
COPY auth-service/src ./auth-service/src
RUN mvn -o -q -DskipTests -f auth-service/pom.xml package
FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /app/auth-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]
