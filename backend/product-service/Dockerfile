FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY . .
RUN mvn -B -q -DskipTests -pl product-service -am package

FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /app/product-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]
