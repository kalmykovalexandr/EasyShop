FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /src

COPY pom.xml ./pom.xml
COPY common-web/pom.xml ./common-web/pom.xml
COPY common-security/pom.xml ./common-security/pom.xml
COPY api-gateway/pom.xml ./api-gateway/pom.xml
COPY auth-service/pom.xml ./auth-service/pom.xml
COPY product-service/pom.xml ./product-service/pom.xml
COPY order-service/pom.xml ./order-service/pom.xml

RUN mvn -B -q -ntp -DskipTests -pl order-service -am dependency:go-offline
COPY . .
RUN mvn -B -ntp -DskipTests -pl order-service -am package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/order-service/target/app.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]