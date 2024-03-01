# Stage 1: Build the application
FROM maven:3.8.4-openjdk-17-slim AS build
# Copy local code to the container image
COPY src /home/app/src
COPY pom.xml /home/app
# Build a release artifact.
RUN mvn -f /home/app/pom.xml clean package

# Stage 2: Setup the runtime environment
FROM openjdk:17-slim

# Copy the built application JAR from the build stage
COPY --from=build /home/app/target/*.jar app.jar

# Start the application
ENTRYPOINT ["java", "-jar", "/app.jar"]