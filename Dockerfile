FROM openjdk:17-slim

ARG VERSION

EXPOSE 8080

ADD target/usermicroservice-$VERSION.jar app.jar

CMD ["java", "-jar", "app.jar"]