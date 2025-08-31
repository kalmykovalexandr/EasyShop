FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./pom.xml
COPY product-service/pom.xml ./product-service/pom.xml
RUN mvn -q -DskipTests -f product-service/pom.xml dependency:go-offline

COPY product-service/src ./product-service/src
RUN mvn -q -DskipTests -f product-service/pom.xml package

FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /app/product-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]
