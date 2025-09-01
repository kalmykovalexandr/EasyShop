FROM easyshop-build-base:1.0.0 AS build
WORKDIR /app
COPY product-service/pom.xml ./product-service/pom.xml
COPY product-service/src ./product-service/src
RUN mvn -o -q -DskipTests -f product-service/pom.xml package
FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /app/product-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]
