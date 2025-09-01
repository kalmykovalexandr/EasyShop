FROM easyshop-build-base:1.0.0 AS build
WORKDIR /app

COPY auth-service/pom.xml ./auth-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests -f auth-service/pom.xml dependency:go-offline
COPY auth-service/src ./auth-service/src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests -f auth-service/pom.xml package

FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /app/auth-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]
