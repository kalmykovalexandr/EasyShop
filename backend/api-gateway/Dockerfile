FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./pom.xml
COPY api-gateway/pom.xml ./api-gateway/pom.xml
RUN mvn -q -DskipTests -f api-gateway/pom.xml dependency:go-offline
COPY api-gateway/src ./api-gateway/src
RUN mvn -q -DskipTests -f api-gateway/pom.xml package
FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /app/api-gateway/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]
