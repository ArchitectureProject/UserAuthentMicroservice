# Stage 1: Build the application
FROM maven:3.8.4-openjdk-17-slim AS build
# Copy local code to the container image
COPY src /home/app/src
COPY pom.xml /home/app
# Build a release artifact.
RUN mvn -f /home/app/pom.xml clean package

# Stage 2: Setup the runtime environment
FROM openjdk:17-slim

# Install dockerize
ENV DOCKERIZE_VERSION v0.6.1
RUN apt-get update && apt-get install -y wget && \
    wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz && \
    tar -C /usr/local/bin -xzvf dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz && \
    rm dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Copy the built application JAR from the build stage
COPY --from=build /home/app/target/*.jar app.jar

# Use dockerize to wait for dependencies to be up before starting the application
ENTRYPOINT ["dockerize", "-wait", "tcp://dbhost:dbport", "-timeout", "30s", "java", "-jar", "/app.jar"]