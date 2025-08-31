FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./pom.xml
COPY auth-service/pom.xml ./auth-service/pom.xml
RUN mvn -q -DskipTests -f auth-service/pom.xml dependency:go-offline

COPY auth-service/src ./auth-service/src
RUN mvn -q -DskipTests -f auth-service/pom.xml package

FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /app/auth-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]
