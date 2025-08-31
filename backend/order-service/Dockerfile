FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./pom.xml
RUN mvn -q -N -DskipTests install
COPY common-security/pom.xml ./common-security/pom.xml
COPY common-security/src ./common-security/src
COPY order-service/pom.xml ./order-service/pom.xml
RUN mvn -q -DskipTests -f common-security/pom.xml install
RUN mvn -q -DskipTests -f order-service/pom.xml dependency:go-offline

COPY order-service/src ./order-service/src
RUN mvn -q -DskipTests -f order-service/pom.xml package

FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /app/order-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]
