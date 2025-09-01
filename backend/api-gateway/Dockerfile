FROM easyshop-build-base:latest AS build
WORKDIR /app
COPY api-gateway/pom.xml ./api-gateway/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests -f api-gateway/pom.xml dependency:go-offline
COPY api-gateway/src ./api-gateway/src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests -f api-gateway/pom.xml package
FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /app/api-gateway/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]
