FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY . .
RUN mvn -B -q -DskipTests -pl order-service -am package

FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /app/order-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]
