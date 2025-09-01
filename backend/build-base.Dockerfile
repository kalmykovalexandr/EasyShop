FROM maven:3.9-eclipse-temurin-21 AS build

# Credentials for GitHub Packages are passed securely at build time
ARG GITHUB_ACTOR
ARG GITHUB_TOKEN

# Copy Maven settings with placeholders for the credentials
COPY settings.xml /usr/share/maven/ref/settings.xml

COPY pom.xml ./pom.xml
COPY common-security/ ./common-security/

# Use the custom settings file for resolving dependencies
RUN --mount=type=cache,target=/root/.m2 mvn --settings /usr/share/maven/ref/settings.xml -q -DskipTests -pl common-security install
