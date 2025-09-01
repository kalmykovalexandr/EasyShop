FROM easyshop-build-base:latest AS build
WORKDIR /app

COPY product-service/pom.xml ./product-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests -f product-service/pom.xml dependency:go-offline
COPY product-service/src ./product-service/src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests -f product-service/pom.xml package

FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /app/product-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]
