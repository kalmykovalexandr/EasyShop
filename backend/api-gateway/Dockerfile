FROM easyshop-build-base:1.0.0 AS build
ARG GITHUB_ACTOR
ARG GITHUB_TOKEN
WORKDIR /app
COPY api-gateway/pom.xml ./api-gateway/pom.xml
COPY api-gateway/src ./api-gateway/src
RUN mvn -o -q -DskipTests -f api-gateway/pom.xml package
FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /app/api-gateway/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]
