# Build the Spring Boot JAR, including the React production bundle created by the Maven frontend plugin.
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app
COPY . .
RUN mvn -DskipTests clean package

# Run only the packaged application in a smaller Java 21 runtime image.
FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/target/simple-bank-app-0.0.1-SNAPSHOT.jar app.jar

# Render's default web-service port is 10000; Spring reads the actual value from PORT.
EXPOSE 10000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
