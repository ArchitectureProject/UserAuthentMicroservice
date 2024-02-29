# Use the official Maven image for a build stage
FROM maven:3.8.4-openjdk-17-slim AS build
# Copy local code to the container image
COPY src /home/app/src
COPY pom.xml /home/app
# Build a release artifact.
RUN mvn -f /home/app/pom.xml clean package

# Use OpenJDK for runtime
FROM openjdk:17-slim
COPY --from=build /home/app/target/*.jar app.jar
# Run the web service on container startup.
ENTRYPOINT ["java","-jar","/app.jar"]
